package com.example.babyhands.config;

import com.example.babyhands.security.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.babyhands.security.JwtAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration               // 1. 스프링 컨테이너에 자동 스캔되기 위한 설정
@EnableWebSecurity           // 2. Security 기능 활성화
public class SecurityConfig {

    // Security 기본 필터 대신에 내가 만드는 나만의 필터를 제작!
    // Bean --> 직접적인 제어가 어려운 라이브러리를 사용할 때, 많이 사용
    // --> 메소드의 결과가 반환되면서, 자동으로 SC에 해당하는 객체가 생성되는 특징

    // 3. 수동으로 Bean 생성
    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http, 
        OAuth2SuccessHandler oAuth2SuccessHandler,
        JwtAuthenticationFilter jwtAuthenticationFilter
        ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // React + JWT 방식이라 CSRF 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 사용 시 세션 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/members/login", "/api/members/logout").permitAll() // 로그인 엔드포인트 허용
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll() // OAuth2 엔드포인트 허용
                        .requestMatchers("/api/tests/**").hasRole("USER")
                        .anyRequest().authenticated() // 테스트 제출은 인증 필요
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // JWT 필터 추가 (권한 체크 전에 실행)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler) // OAuth2 로그인 성공 핸들러
                        .failureUrl("http://localhost:5173/login?error=oauth2_failed") // 실패 시 리다이렉트
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "babyhands-front.vercel.app")); // React 개발 서버, 운영 서버
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // SecurityConfig.java
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
