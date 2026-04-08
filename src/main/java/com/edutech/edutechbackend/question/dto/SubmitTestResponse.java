package com.edutech.edutechbackend.question.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitTestResponse {

    private Long testId;
    private Integer totalQuestions;
    private Integer attemptedQuestions;
    private Integer correctAnswers;
    private Integer wrongAnswers;
    private Integer score;
    private Integer totalMarks;
}