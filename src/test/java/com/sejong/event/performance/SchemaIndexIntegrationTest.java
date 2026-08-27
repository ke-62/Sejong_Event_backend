package com.sejong.event.performance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 엔티티에 선언한 복합 인덱스가 실제 스키마에 생성되는지 검증한다.
 * JDBC 표준 {@link java.sql.DatabaseMetaData#getIndexInfo}로 조회하므로 H2·MySQL 공통.
 */
@SpringBootTest
@ActiveProfiles("test")
class SchemaIndexIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @ParameterizedTest(name = "{0} 에 인덱스 {1}")
    @CsvSource({
            "ATTENDANCE_LOGS, IDX_ATTENDANCE_EVENT_STATUS",
            "ATTENDANCE_LOGS, IDX_ATTENDANCE_EVENT_CREATED",
            "WAITING_ENTRIES, IDX_WAITING_BOOTH_STATUS",
            "WAITING_ENTRIES, IDX_WAITING_BOOTH_ORDER",
            "BOOTHS,          IDX_BOOTH_STATUS_WAITING",
            "BOOTHS,          IDX_BOOTH_CATEGORY_STATUS",
            "BOOTHS,          IDX_BOOTH_ORGANIZATION",
            "EVENTS,          IDX_EVENT_ACTIVE",
            "EVENTS,          IDX_EVENT_ORGANIZATION",
    })
    @DisplayName("선언한 복합 인덱스가 스키마에 존재한다")
    void indexExists(String table, String indexName) throws Exception {
        Set<String> indexes = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             ResultSet rs = connection.getMetaData()
                     .getIndexInfo(null, null, table.trim(), false, false)) {
            while (rs.next()) {
                String name = rs.getString("INDEX_NAME");
                if (name != null) {
                    indexes.add(name.toUpperCase());
                }
            }
        }
        assertThat(indexes).contains(indexName.trim().toUpperCase());
    }
}
