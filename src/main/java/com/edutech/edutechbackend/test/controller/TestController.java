package com.edutech.edutechbackend.test.controller;

import com.edutech.edutechbackend.question.dto.StudentQuestionResponse;
import com.edutech.edutechbackend.question.dto.SubmitTestRequest;
import com.edutech.edutechbackend.question.dto.SubmitTestResponse;
import com.edutech.edutechbackend.test.dto.TestCreateRequest;
import com.edutech.edutechbackend.test.dto.TestResponse;
import com.edutech.edutechbackend.test.enums.TestTypeFilter;
import com.edutech.edutechbackend.test.service.TestService;
import com.edutech.edutechbackend.testattempt.dto.TestAttemptResponse;
import com.edutech.edutechbackend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @GetMapping
    public List<TestResponse> getTests(@RequestParam(defaultValue = "ALL") TestTypeFilter type) {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return testService.getTestsByType(user, type);
    }

    @PostMapping("/create")
    public TestResponse createTest(@Valid @RequestBody TestCreateRequest request) {
        User mentor = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return testService.createTest(mentor, request);
    }

    @GetMapping("/{testId}/questions")
    public List<StudentQuestionResponse> getQuestionsForSelectedTest(@PathVariable Long testId) {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return testService.getQuestionsForSelectedTest(user, testId);
    }

    @PostMapping("/{testId}/submit")
    public SubmitTestResponse submitTest(
            @PathVariable Long testId,
            @Valid @RequestBody SubmitTestRequest request
    ) {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return testService.submitTest(user, testId, request);
    }

    @GetMapping("/my-attempts")
    public List<TestAttemptResponse> getMyAttempts() {
        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return testService.getMyAttempts(user);
    }
}