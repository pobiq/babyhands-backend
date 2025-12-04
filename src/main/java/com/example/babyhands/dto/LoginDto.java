package com.example.babyhands.dto;

import com.example.babyhands.entity.MemberEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class LoginDto {

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String loginId;
        private String password;
    }


    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String nickname;
        private String accessToken;

        public static Response of(MemberEntity entity, String accessToken) {
            return Response.builder()
                    .accessToken(accessToken)
                    .nickname(entity.getNickname())
                    .build();
        }
    }

}