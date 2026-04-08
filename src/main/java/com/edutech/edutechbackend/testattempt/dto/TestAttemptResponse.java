package com.edutech.edutechbackend.testattempt.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TestAttemptResponse {

    private Long attemptId;
    private Long testId;
    private String testTitle;
    private Integer totalQuestions;
    private Integer attemptedQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Integer score;
    private Integer totalMarks;
    private LocalDateTime submittedAt;
}