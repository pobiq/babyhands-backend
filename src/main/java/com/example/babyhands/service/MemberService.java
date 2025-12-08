package com.example.babyhands.service;

import com.example.babyhands.dto.LoginDto;
import com.example.babyhands.dto.LoginDto.Request;
import com.example.babyhands.entity.MemberEntity;
import com.example.babyhands.repository.MemberRepository;
import com.example.babyhands.security.TokenProvider;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    /**
     * 회원 로그인
     * @param request 로그인 요청 DTO (loginId, password)
     * @return accessToken
     * @throws RuntimeException 로그인 실패 시 예외 발생
     */
    public LoginDto.Response login(Request request) {
        // 1. loginId로 회원 조회
        Optional<MemberEntity> memberOpt = memberRepository.findByLoginId(request.getLoginId());
        
        if (memberOpt.isEmpty()) {
            throw new RuntimeException("존재하지 않는 회원입니다.");
        }
        
        MemberEntity member = memberOpt.get();
        
        // 2. 비밀번호 검증
        if (!member.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = tokenProvider.generateAccessToken(member.getLoginId());

        LoginDto.Response result = LoginDto.Response.of(member, accessToken);

        return result;
    }

    /**
     * 회원 로그아웃
     * JWT 기반이므로 클라이언트에서 토큰 삭제가 주된 작업이지만,
     * 향후 토큰 블랙리스트 관리 등을 위해 API로 제공
     */
    public void logout() {
        // 현재는 단순히 성공 응답만 반환
        // 향후 토큰 블랙리스트 관리 시 여기에 로직 추가 가능
    }

    

}
