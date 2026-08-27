package com.sejong.event.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 운영 프로필 설정이 안전한 값을 쓰는지 검증한다.
 * (MySQL 이 필요 없도록 컨텍스트를 띄우지 않고 yml 만 로드한다.)
 */
class ProdConfigTest {

    private static final String PROD_YML = "application-prod.yml";

    @Test
    @DisplayName("운영 프로필은 ddl-auto=validate, show-sql=false")
    void prodUsesSafeJpaSettings() throws IOException {
        PropertySource<?> source = loadProd();

        assertThat(source.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(source.getProperty("spring.jpa.show-sql")).isEqualTo(false);
    }

    @Test
    @DisplayName("운영 프로필은 DB 비밀번호·JWT 서명 키에 하드코딩 기본값이 없다")
    void prodHasNoHardcodedSecrets() throws IOException {
        String yml = new String(new ClassPathResource(PROD_YML).getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);

        assertThat(yml).contains("${DB_PASSWORD}").doesNotContain("${DB_PASSWORD:");
        assertThat(yml).contains("${JWT_SECRET}").doesNotContain("${JWT_SECRET:");
        assertThat(yml).contains("${DB_URL}").doesNotContain("${DB_URL:");
        assertThat(yml).contains("${CORS_ALLOWED_ORIGINS}").doesNotContain("${CORS_ALLOWED_ORIGINS:");
    }

    @Test
    @DisplayName("운영 프로필은 Swagger 를 기본 비활성한다")
    void prodDisablesSwaggerByDefault() throws IOException {
        PropertySource<?> source = loadProd();

        assertThat(source.getProperty("springdoc.api-docs.enabled")).isEqualTo("${SWAGGER_ENABLED:false}");
        assertThat(source.getProperty("springdoc.swagger-ui.enabled")).isEqualTo("${SWAGGER_ENABLED:false}");
    }

    private PropertySource<?> loadProd() throws IOException {
        List<PropertySource<?>> sources =
                new YamlPropertySourceLoader().load("prod", new ClassPathResource(PROD_YML));
        return sources.get(0);
    }
}
