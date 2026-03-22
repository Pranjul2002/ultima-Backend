package com.edutech.edutechbackend.controller;

import com.edutech.edutechbackend.dto.*;
import com.edutech.edutechbackend.service.SubjectService;
import com.edutech.edutechbackend.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tests")
// ↑ all student-facing test endpoints
//   /api/tests/** is protected by anyRequest().authenticated()
//   both STUDENT and ADMIN roles can access
@RequiredArgsConstructor
public class StudentTestController {

    private final TestService testService;
    private final SubjectService subjectService;

    // ── GET /api/tests/subjects ───────────────────────────────────────────
    // Student browses all available subjects
    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectResponseDTO>> getAllSubjects() {

        return ResponseEntity.ok(subjectService.getAllSubjects());
        // ↑ returns [Math, Science, English, ...]
        //   student picks a subject to browse tests under it
        //   no authentication details needed
        //   all subjects visible to everyone logged in
    }

    // ── GET /api/tests/papers?subjectId=1 ────────────────────────────────
    // Student sees all test papers under a subject
    @GetMapping("/papers")
    public ResponseEntity<List<TestResponseDTO>> getTestsBySubject(
            @RequestParam Long subjectId) {
        // ↑ @RequestParam reads from URL query string
        //   /api/tests/papers?subjectId=1
        //   subjectId = 1

        List<TestResponseDTO> tests = testService.getTestsBySubject(subjectId);

        return ResponseEntity.ok(tests);
        // ↑ returns all test papers under Math:
        // [
        //   { id:1, title:"Math Mid-Term", createdBy:"Raj", totalQuestions:10 },
        //   { id:2, title:"Math Final",    createdBy:"Raj", totalQuestions:15 },
        //   { id:3, title:"Math Practice", createdBy:"Priya", totalQuestions:8 }
        // ]
    }

    // ── GET /api/tests/papers/{testId}/questions ──────────────────────────
    // Student opens a test paper and sees all questions
    @GetMapping("/papers/{testId}/questions")
    public ResponseEntity<List<QuestionResponseDTO>> getQuestionsForTest(
            @PathVariable Long testId) {
        // ↑ /api/tests/papers/1/questions
        //   testId = 1

        List<QuestionResponseDTO> questions =
                testService.getQuestionsForTest(testId);

        return ResponseEntity.ok(questions);
        // ↑ returns all questions WITHOUT correct answers:
        // [
        //   { id:1, questionText:"What is 2+2?", optionA:"3", optionB:"4"... },
        //   { id:2, questionText:"What is 5×5?", optionA:"20", optionB:"25"... }
        // ]
        // student reads questions, selects answers in frontend
        // then hits submit
    }

    // ── POST /api/tests/submit ────────────────────────────────────────────
    // Student submits their answers for scoring
    @PostMapping("/submit")
    public ResponseEntity<TestResultResponseDTO> submitTest(
            @Valid @RequestBody SubmitTestRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // ↑ we need to know WHO submitted
        //   to save the TestSession under correct student

        TestResultResponseDTO result = testService.submitTest(
                request,
                userDetails.getUsername()
                // ↑ student's email from JWT token
        );

        return ResponseEntity.ok(result);
        // ↑ returns score + detailed answer breakdown:
        // {
        //   "sessionId": 1,
        //   "testTitle": "Math Mid-Term 2026",
        //   "score": 7,
        //   "totalQuestions": 10,
        //   "percentage": 70.0,
        //   "answerResults": [
        //     { "questionId":1, "selectedOption":"B",
        //       "correctOption":"B", "correct":true },
        //     { "questionId":2, "selectedOption":"A",
        //       "correctOption":"B", "correct":false }
        //   ]
        // }
    }

    // ── GET /api/tests/history ────────────────────────────────────────────
    // Student views all their past test attempts
    @GetMapping("/history")
    public ResponseEntity<List<TestHistoryResponseDTO>> getTestHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<TestHistoryResponseDTO> history = testService.getTestHistory(
                userDetails.getUsername()
        );

        return ResponseEntity.ok(history);
        // ↑ returns newest attempts first:
        // [
        //   { testTitle:"Math Final",    score:9,  percentage:90.0, takenAt:"2026-03-10" },
        //   { testTitle:"Math Mid-Term", score:7,  percentage:70.0, takenAt:"2026-03-01" },
        //   { testTitle:"Math Practice", score:6,  percentage:75.0, takenAt:"2026-02-20" }
        // ]
    }
}