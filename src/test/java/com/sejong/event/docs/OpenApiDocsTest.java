package com.sejong.event.docs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OpenAPI 명세 최종 검토.
 * - 실시간 스펙이 전 태그·핵심 경로·Bearer 스키마를 담는지 검증
 * - 커밋된 docs/openapi.yaml 스냅샷이 실제 경로를 빠짐없이 담는지 검증
 *   (재생성: ./gradlew test -Dopenapi.update=true)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocsTest {

    private static final Path SNAPSHOT = Path.of("docs/openapi.yaml");
    private static final Pattern PATH_LINE = Pattern.compile("(?m)^ {2}(/[^:\\n]+):");

    @Autowired
    private MockMvc mockMvc;

    private String fetchSpec() throws Exception {
        return mockMvc.perform(get("/api-docs.yaml"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("OpenAPI 스펙은 전 태그·핵심 경로·Bearer 보안 스키마를 담는다")
    void specIsComplete() throws Exception {
        String spec = fetchSpec();

        assertThat(spec).contains("세종대학교 학내 행사 통합 관리 플랫폼 API");
        for (String tag : List.of("Auth", "Events", "Attendance", "Booths", "Waiting", "Accounts", "Festival")) {
            assertThat(spec).as("태그 " + tag).contains(tag);
        }
        for (String path : List.of(
                "/api/auth/login",
                "/api/events/{eventId}/qr-token",
                "/api/events/{eventId}/lottery",
                "/api/events/{eventId}/attendance/scan",
                "/api/booths/{boothId}/approve",
                "/api/booths/{boothId}/waiting/{waitingId}",
                "/api/accounts",
                "/api/festival/status")) {
            assertThat(spec).as("경로 " + path).contains(path + ":");
        }
        assertThat(spec).contains("BearerAuth");
    }

    @Test
    @DisplayName("커밋된 docs/openapi.yaml 은 실제 API 경로를 빠짐없이 담는다")
    void committedSnapshotCoversAllPaths() throws Exception {
        String live = fetchSpec();

        if (Boolean.getBoolean("openapi.update")) {
            Files.createDirectories(SNAPSHOT.getParent());
            Files.writeString(SNAPSHOT, live);
        }

        assertThat(SNAPSHOT).as("docs/openapi.yaml 존재 (재생성: -Dopenapi.update=true)").exists();
        Set<String> livePaths = paths(live);
        Set<String> committedPaths = paths(Files.readString(SNAPSHOT));

        assertThat(committedPaths)
                .as("커밋된 스냅샷의 경로 목록이 실제와 일치")
                .isEqualTo(livePaths);
    }

    private static Set<String> paths(String yaml) {
        Set<String> result = new TreeSet<>();
        Matcher matcher = PATH_LINE.matcher(yaml);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }
}
