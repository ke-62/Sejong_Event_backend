package com.sejong.event.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 자주 조회되고 변경이 드문 데이터에 대한 인메모리 캐시.
 * 쓰기 시점에 서비스에서 @CacheEvict 로 무효화하므로 별도 TTL 은 두지 않는다.
 *
 * - festivalStatus : 축제 활성 여부 (관리자 토글 시에만 변경)
 * - events         : 행사 단건 조회 (id 별)
 * - eventList      : 행사 목록 조회 (역할·사용자 별)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String FESTIVAL_STATUS = "festivalStatus";
    public static final String EVENTS = "events";
    public static final String EVENT_LIST = "eventList";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(FESTIVAL_STATUS, EVENTS, EVENT_LIST);
    }
}
