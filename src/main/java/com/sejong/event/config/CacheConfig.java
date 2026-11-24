package com.sejong.event.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * 자주 조회되고 변경이 드문 데이터에 대한 인메모리 캐시 (Caffeine).
 * CacheManager 는 Spring Boot 자동 설정이 만든다 — application.yml 의 spring.cache.* 참고.
 * 직접 빈을 만들지 않는 이유: spring.cache.type=none 으로 캐시를 끄고 전후를 비교할 수 있게 하고,
 * Caffeine 통계(recordStats)가 Actuator 지표(cache.gets 등)로 자동 등록되게 하기 위함.
 *
 * - festivalStatus : 축제 활성 여부 (관리자 토글 시 무효화)
 * - events         : 행사 단건 조회 (id 별, 수정·삭제·입장 시 무효화)
 * - eventList      : 행사 목록 조회 (역할·사용자 별, 생성·수정·삭제·입장 시 전체 무효화)
 *
 * 부스 대기 인원·대기열은 등록·호출·취소마다 바뀌어 캐시 대상에서 제외 (WebSocket 브로드캐스트로 처리).
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String FESTIVAL_STATUS = "festivalStatus";
    public static final String EVENTS = "events";
    public static final String EVENT_LIST = "eventList";
}
