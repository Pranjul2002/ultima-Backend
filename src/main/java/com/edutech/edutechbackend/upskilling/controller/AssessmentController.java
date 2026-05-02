package com.edutech.edutechbackend.upskilling.controller;

import com.edutech.edutechbackend.upskilling.dto.*;
import com.edutech.edutechbackend.upskilling.service.AssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints for the AI-powered assessment flow:
 *
 *  POST /api/assessment/generate   → generate questions from a source PDF
 *  POST /api/assessment/evaluate   → evaluate student answers (text or handwritten PDF/image)
 *
 * Both endpoints are PUBLIC (no JWT required) — same as /api/chat/**
 * Add to SecurityConfig: .requestMatchers("/api/assessment/**").permitAll()
 */
@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    /**
     * Generate a set of questions from the already-uploaded source PDF.
     *
     * Request body:
     * {
     *   "sourceId": 42,
     *   "count": 5,          // optional, default 5
     *   "topic": "Chapter 2" // optional focus topic
     * }
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateQuestions(
            @Valid @RequestBody GenerateQuestionsRequest request
    ) {
        try {
            GenerateQuestionsResponse response = assessmentService.generateQuestions(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Evaluate student answers against the source PDF.
     *
     * Accepts EITHER typed text OR a base64-encoded handwritten PDF/image per answer.
     *
     * Request body:
     * {
     *   "sourceId": 42,
     *   "answers": [
     *     {
     *       "questionNumber": 1,
     *       "questionText": "What is photosynthesis?",
     *       "textAnswer": "Photosynthesis is..."          // typed answer
     *     },
     *     {
     *       "questionNumber": 2,
     *       "questionText": "Explain the water cycle.",
     *       "handwrittenBase64": "<base64 string>",       // handwritten image/PDF
     *       "handwrittenMimeType": "image/jpeg"
     *     }
     *   ]
     * }
     */
    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluateAnswers(
            @Valid @RequestBody EvaluateAnswersRequest request
    ) {
        try {
            EvaluationResult result = assessmentService.evaluateAnswers(request);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}