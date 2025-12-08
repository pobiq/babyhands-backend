package com.example.babyhands.dto;

import com.example.babyhands.entity.MemberEntity;
import com.example.babyhands.entity.SignLanguageEntity;
import lombok.*;

import java.util.List;

public class TestListDto {

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long sl_id;
        private String meaning;
        private String video_path;
        private List<String> answers;

        public static TestListDto.Response of(SignLanguageEntity entity, List<String> answers) {
            return Response.builder()
                    .sl_id(entity.getId())
                    .meaning(entity.getMeaning())
                    .video_path(entity.getVideoPath())
                    .answers(answers)
                    .build();
        }
    }

}
