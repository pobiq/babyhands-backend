package com.example.babyhands.security;

import com.example.babyhands.entity.MemberEntity;
import com.example.babyhands.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();
        
        // OPTIONS 요청은 필터를 통과시킴 (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(requestMethod)) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("JWT Filter - Request: " + requestMethod + " " + requestPath);

        // Authorization 헤더에서 토큰 추출
        String token = extractToken(request);

        if (token != null) {
            System.out.println("JWT Filter - Token found, length: " + token.length());
            // 토큰 검증 및 loginId 추출
            String loginId = tokenProvider.validateToken(token);

            if (loginId != null) {
                System.out.println("JWT Filter - Token validated, loginId: " + loginId);
                // MemberEntity 조회 (roles도 함께 조회됨)
                memberRepository.findByLoginId(loginId).ifPresent(member -> {
                    // 권한 정보 생성 (roles가 null이거나 비어있을 경우 빈 리스트 반환)
                    List<SimpleGrantedAuthority> authorities = (member.getRoles() != null && !member.getRoles().isEmpty())
                            ? member.getRoles().stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList())
                            : List.of();

                    // Authentication 객체 생성
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            member,
                            null,
                            authorities
                    );

                    // SecurityContext에 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 디버깅 로그
                    System.out.println("JWT Authentication successful for user: " + loginId);
                    System.out.println("Authorities: " + authorities);
                });
            } else {
                System.out.println("JWT Filter - Token validation failed for path: " + requestPath);
            }
        } else {
            System.out.println("JWT Filter - No JWT Token found in request for path: " + requestPath);
            String authHeader = request.getHeader("Authorization");
            System.out.println("JWT Filter - Authorization header: " + (authHeader != null ? "present" : "null"));
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}