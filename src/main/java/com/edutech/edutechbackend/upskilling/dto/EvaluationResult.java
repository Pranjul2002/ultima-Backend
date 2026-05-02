package com.edutech.edutechbackend.upskilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResult {

    private Long sourceId;
    private int totalQuestions;
    private int correctCount;
    private int wrongCount;
    private int score; // 0–100

    private List<QuestionResult> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResult {
        private int questionNumber;
        private String questionText;
        private String studentAnswer;   // transcribed or typed answer
        private boolean correct;
        private String feedback;        // explanation of why correct/wrong
        private String correctAnswer;   // brief correct answer from the document
    }
}