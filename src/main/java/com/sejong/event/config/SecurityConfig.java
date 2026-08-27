package com.sejong.event.config;

import com.sejong.event.auth.jwt.JwtAuthenticationFilter;
import com.sejong.event.auth.jwt.RestAccessDeniedHandler;
import com.sejong.event.auth.jwt.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // 공개 — Swagger, WebSocket
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        // 인증 API
                        .requestMatchers("/api/auth/**").permitAll()

                        // 축제 상태 조회 — 공개
                        .requestMatchers(HttpMethod.GET, "/api/festival/status").permitAll()

                        // 행사 목록·단건 조회 — 공개 (서비스에서 역할에 따라 필터링)
                        .requestMatchers(HttpMethod.GET, "/api/events").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()

                        // 웨이팅 목록(GET /booths/{id}/waiting)은 인증 필요 — booths/** permitAll 보다 먼저 선언
                        .requestMatchers(HttpMethod.GET, "/api/booths/*/waiting").authenticated()

                        // 부스 목록·단건 조회 — 공개
                        .requestMatchers(HttpMethod.GET, "/api/booths").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/booths/**").permitAll()

                        // 웨이팅 등록·상태 조회 — 공개
                        .requestMatchers(HttpMethod.POST, "/api/booths/*/waiting").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/booths/*/waiting/*").permitAll()

                        // QR 스캔 입장 — 공개
                        .requestMatchers(HttpMethod.POST, "/api/events/*/attendance/scan").permitAll()

                        // superuser 전용
                        .requestMatchers("/api/accounts/**").hasRole("SUPERUSER")
                        .requestMatchers(HttpMethod.PATCH, "/api/festival/status").hasRole("SUPERUSER")
                        .requestMatchers(HttpMethod.PATCH, "/api/booths/*/approve").hasRole("SUPERUSER")
                        .requestMatchers(HttpMethod.PATCH, "/api/booths/*/reject").hasRole("SUPERUSER")

                        // 나머지는 인증 필요 (organizer 이상)
                        .anyRequest().authenticated()
                )
                // ExceptionTranslationFilter 뒤·AuthorizationFilter 앞에 배치 —
                // 필터가 던지는 InvalidTokenException 을 ExceptionTranslationFilter 가 잡아 401 로 변환한다.
                .addFilterBefore(jwtAuthenticationFilter, AuthorizationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/ws/**", configuration);
        return source;
    }
}
