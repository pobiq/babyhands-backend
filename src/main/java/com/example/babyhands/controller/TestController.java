package com.example.babyhands.controller;

import com.example.babyhands.dto.TestDto;
import com.example.babyhands.dto.TestListDto;
import com.example.babyhands.entity.MemberEntity;
import com.example.babyhands.service.TestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {
    
    private final TestService testService;

    @GetMapping("/getTestList")
    public ResponseEntity<List<TestListDto.Response>> submitTest() {

        List<TestListDto.Response> result = testService.getTestList();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitTest(
            @RequestBody TestDto.Request request,
            @AuthenticationPrincipal MemberEntity member) {
        
        String result = testService.submitTest(request, member);
        
        return ResponseEntity.ok(result);
    }
}

