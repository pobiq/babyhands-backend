package com.example.babyhands.security;

import com.example.babyhands.entity.AttendanceEntity;
import com.example.babyhands.entity.MemberEntity;
import com.example.babyhands.repository.AttendanceRepository;
import com.example.babyhands.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // OAuth2 제공자 정보 추출 (요청 URI에서 추출)
        String requestUri = request.getRequestURI();
        String registrationId = "google"; // 기본값
        
        if (requestUri.contains("/kakao")) {
            registrationId = "kakao";
        } else if (requestUri.contains("/naver")) {
            registrationId = "naver";
        } else if (requestUri.contains("/google")) {
            registrationId = "google";
        }
        
        // OAuth2 사용자 정보 추출 (제공자별로 다름)
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractEmail(attributes, registrationId);
        String name = extractName(attributes, registrationId);
        String nickname = extractNickname(attributes, registrationId);
        
        // 이메일이 없으면 에러 처리
        if (email == null || email.isEmpty()) {
            getRedirectStrategy().sendRedirect(request, response, 
                "http://localhost:5173/login?error=email_required");
            return;
        }
        
        // 소셜 로그인 처리 (회원이 없으면 자동 가입)
        MemberEntity member = processSocialLogin(email, name, nickname, registrationId);

        // 로그인 성공 시 출석 체크(하루에 한번만)
        if(!attendanceRepository.existsByLoginDateAndMemberId(LocalDate.now(), member.getId())) {
            AttendanceEntity attendance = AttendanceEntity.builder()
                    .loginDate(LocalDate.now())
                    .member(member)
                    .build();

            attendanceRepository.save(attendance);
        }

        // JWT 토큰 생성
        String accessToken = tokenProvider.generateAccessToken(member.getLoginId());
        
        // 닉네임을 URL 인코딩 (한글 등 특수문자 처리)
        String encodedNickname = URLEncoder.encode(
            member.getNickname() != null ? member.getNickname() : "", 
            StandardCharsets.UTF_8
        );
        
        // UriComponentsBuilder로 안전하게 URL 생성 및 인코딩
        String redirectUrl = UriComponentsBuilder
            .fromUriString("http://localhost:5173/login/oauth2/callback")
            .queryParam("token", accessToken)
            .queryParam("nickname", member.getNickname() != null ? member.getNickname() : "")
            .build()
            .encode(StandardCharsets.UTF_8)
            .toUriString();

        response.sendRedirect(redirectUrl);
    }

    /**
     * 소셜 로그인 처리 (회원이 없으면 자동 가입)
     */
    private MemberEntity processSocialLogin(String email, String name, String nickname, String provider) {
        // email을 loginId로 사용
        Optional<MemberEntity> memberOpt = memberRepository.findByLoginId(email);
        
        if (memberOpt.isPresent()) {
            // 기존 회원이면 그대로 반환
            return memberOpt.get();
        } else {
            // 신규 회원이면 자동 가입
            // 비밀번호를 BCrypt로 해시화하여 저장
            String rawPassword = "SOCIAL_LOGIN_" + provider;
            String encodedPassword = passwordEncoder.encode(rawPassword);
            
            MemberEntity newMember = MemberEntity.builder()
                    .loginId(email)
                    .password(encodedPassword) // 해시화된 비밀번호 저장
                    .email(email)
                    .name(name)
                    .nickname(nickname != null ? nickname : name)
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build();
            
            return memberRepository.save(newMember);
        }
    }

    /**
     * 제공자별 이메일 추출
     */
    private String extractEmail(Map<String, Object> attributes, String provider) {
        switch (provider) {
            case "google":
                return (String) attributes.get("email");
            case "kakao":
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
                // 카카오의 경우 로그인시 email이 없음
                if (email == null || email.isEmpty()) {
                    Object id = attributes.get("id");
                    return id + "@kakao.com";
                }
                return email;
            case "naver":
                Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
                return naverResponse != null ? (String) naverResponse.get("email") : null;
            default:
                return (String) attributes.get("email");
        }
    }

    /**
     * 제공자별 이름 추출
     */
    private String extractName(Map<String, Object> attributes, String provider) {
        switch (provider) {
            case "google":
                return (String) attributes.get("name");
            case "kakao":
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                if (kakaoAccount != null) {
                    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                    return profile != null ? (String) profile.get("nickname") : null;
                }
                return null;
            case "naver":
                Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
                return naverResponse != null ? (String) naverResponse.get("name") : null;
            default:
                return (String) attributes.get("name");
        }
    }

    /**
     * 제공자별 닉네임 추출
     */
    private String extractNickname(Map<String, Object> attributes, String provider) {
        switch (provider) {
            case "google":
                return (String) attributes.get("name");
            case "kakao":
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                if (kakaoAccount != null) {
                    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                    return profile != null ? (String) profile.get("nickname") : null;
                }
                return null;
            case "naver":
                Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
                return naverResponse != null ? (String) naverResponse.get("nickname") : null;
            default:
                return (String) attributes.get("name");
        }
    }
}

