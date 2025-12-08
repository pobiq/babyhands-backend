package com.example.babyhands.service;

import com.example.babyhands.dto.TestDto;
import com.example.babyhands.dto.TestListDto;
import com.example.babyhands.entity.MemberEntity;
import com.example.babyhands.entity.SignLanguageEntity;
import com.example.babyhands.entity.SLTestEntity;
import com.example.babyhands.repository.MemberRepository;
import com.example.babyhands.repository.SignLanguageRepository;
import com.example.babyhands.repository.SLTestRepository;
import com.example.babyhands.security.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class TestService {
    
    private final SLTestRepository slTestRepository;
    private final MemberRepository memberRepository;
    private final SignLanguageRepository signLanguageRepository;
    private final TokenProvider tokenProvider;

    /**
     * 테스트 5문제 생성
     * @return
     */
    public List<TestListDto.Response> getTestList() {
        List<TestListDto.Response> result = new ArrayList<>();
        List<SignLanguageEntity> signLanguageList = signLanguageRepository.findAll();
        List<Integer> testIdList = new ArrayList<>();
        Random random = new Random();

        while (result.size() < 5) {

            int id_num = random.nextInt(signLanguageList.size()) + 1;

            if(!testIdList.contains(id_num)) {
                testIdList.add(id_num);

                List<String> answers = new ArrayList<>();
                answers.add(signLanguageList.get(id_num-1).getMeaning());

                while(answers.size() < 4) {
                    int answers_num = random.nextInt(signLanguageList.size()) + 1;
                    String answer = signLanguageList.get(answers_num).getMeaning();
                    if(!answers.contains(answer)) {
                        answers.add(answer);
                    }
                }

                Collections.shuffle(answers);

                TestListDto.Response temp = TestListDto.Response.builder()
                        .sl_id((long) id_num)
                        .meaning(signLanguageList.get(id_num-1).getMeaning())
                        .video_path(signLanguageList.get(id_num-1).getVideoPath())
                        .answers(answers)
                        .build();

                result.add(temp);
            }
        }

        return result;
    }

    /**
     * 테스트 5문항 제출
     * @param request
     * @param member
     * @return
     */
    @Transactional
    public String submitTest(TestDto.Request request, MemberEntity member) {
        return null;
    }



}

