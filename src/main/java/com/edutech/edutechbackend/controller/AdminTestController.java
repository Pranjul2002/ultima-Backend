package com.edutech.edutechbackend.controller;

import com.edutech.edutechbackend.dto.*;
import com.edutech.edutechbackend.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tests")
// ↑ all endpoints start with /api/admin/tests
//   automatically protected — only ADMIN role can access
@RequiredArgsConstructor
public class AdminTestController {

    private final TestService testService;

    // ── POST /api/admin/tests ─────────────────────────────────────────────
    // Admin creates a new test paper
    @PostMapping
    public ResponseEntity<TestResponseDTO> createTest(
            @Valid @RequestBody CreateTestRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // ↑ @AuthenticationPrincipal injects the currently
        //   logged in user's UserDetails object
        //   userDetails.getUsername() = their email
        //   extracted from JWT token automatically by JwtAuthFilter
        //
        // We pass email to service so it can find
        // which admin is creating this test
        // and set created_by_id correctly

        TestResponseDTO response = testService.createTest(
                request,
                userDetails.getUsername()
                // ↑ getUsername() returns email
                //   (we set email as username in CustomUserDetailsService)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET /api/admin/tests ──────────────────────────────────────────────
    // Admin sees only their own test papers
    @GetMapping
    public ResponseEntity<List<TestResponseDTO>> getMyTests(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<TestResponseDTO> tests = testService.getMyTests(
                userDetails.getUsername()
        );

        return ResponseEntity.ok(tests);
        // ↑ Admin Raj gets only Raj's tests
        //   Admin Priya gets only Priya's tests
        //   completely isolated
    }

    // ── POST /api/admin/tests/{testId}/questions ──────────────────────────
    // Admin adds a question to one of their test papers
    @PostMapping("/{testId}/questions")
    // ↑ {testId} is a path variable
    //   e.g. POST /api/admin/tests/1/questions
    //   means "add question to test with id=1"
    public ResponseEntity<QuestionResponseDTO> addQuestion(
            @PathVariable Long testId,
            // ↑ extracts "1" from URL and puts it in testId variable

            @Valid @RequestBody CreateQuestionRequestDTO request,

            @AuthenticationPrincipal UserDetails userDetails) {

        QuestionResponseDTO response = testService.addQuestion(
                testId,
                request,
                userDetails.getUsername()
                // ↑ service checks if this admin owns testId
                //   if not → throws exception → 403 Forbidden
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // ── POST /api/admin/tests/{testId}/questions/bulk ─────────────────────
    @PostMapping("/{testId}/questions/bulk")
    public ResponseEntity<List<QuestionResponseDTO>> addQuestions(
            @PathVariable Long testId,
            @Valid @RequestBody List<CreateQuestionRequestDTO> requests,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<QuestionResponseDTO> response = testService.addQuestions(
                testId,
                requests,
                userDetails.getUsername()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}