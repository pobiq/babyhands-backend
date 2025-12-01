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

        public MemberEntity toEntity() {
            return MemberEntity.builder()
                .loginId(loginId)
                .password(password)
                .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        String accessToken;
        
        public LoginResponse.Response toResponse(String accessToken) {
            return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
        }
    }

}