package com.edutech.edutechbackend.question.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Like StudentQuestionResponse but also exposes correctAnswer.
 * Used only by the review endpoint — never during an active attempt.
 */
@Data
@Builder
public class ReviewQuestionResponse {

    private Long    id;
    private String  questionText;
    private String  optionA;
    private String  optionB;
    private String  optionC;
    private String  optionD;
    private Integer marks;
    private String  correctAnswer;   // ← exposed only after attempt is submitted
}