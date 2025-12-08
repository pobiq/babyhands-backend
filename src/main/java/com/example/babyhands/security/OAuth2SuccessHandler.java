package com.example.babyhands.security;

import com.example.babyhands.dto.LoginDto;
import com.example.babyhands.entity.MemberEntity;
import com.example.babyhands.repository.MemberRepository;
import com.example.babyhands.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

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
        
        // JWT 토큰 생성
        String accessToken = tokenProvider.generateAccessToken(member.getLoginId());
        
        // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        String redirectUrl = String.format(
            "http://localhost:5173/login/oauth2/callback?token=%s&nickname=%s",
            accessToken,
            member.getNickname()
        );
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
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
            MemberEntity newMember = MemberEntity.builder()
                    .loginId(email)
                    .password("SOCIAL_LOGIN_" + provider) // 소셜 로그인은 비밀번호 불필요
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
                return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
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

