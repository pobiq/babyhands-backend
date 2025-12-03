package com.example.babyhands.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class TokenProvider {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    // 생성자를 통해 application.yml의 값 주입
    public TokenProvider(
            @Value("${app.auth.token-secret}") String secretKey,
            @Value("${app.auth.access-token-expiration-msec}") long accessTokenExpiration,
            @Value("${app.auth.refresh-token-expiration-msec}") long refreshTokenExpiration) {
        
        // Base64로 인코딩된 secret을 디코딩하여 SecretKey 생성
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // Access Token 생성
    public String generateAccessToken(String loginId) {
        return Jwts.builder()
                .subject(loginId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(String loginId) {
        return Jwts.builder()
                .subject(loginId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    // 토큰 검증 및 subject 반환
    public String validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return claimsJws.getPayload().getSubject();
        } catch (ExpiredJwtException e) {
            // 토큰 만료
            System.out.println("Token expired: " + e.getMessage());
            return null;
        } catch (JwtException e) {
            // 위조, 변조, 기타 오류
            System.out.println("Invalid token: " + e.getMessage());
            return null;
        }
    }
}
