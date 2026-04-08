package com.edutech.edutechbackend.question.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class QuestionCreateRequest {

    @NotBlank
    private String questionText;

    @NotBlank
    private String optionA;

    @NotBlank
    private String optionB;

    @NotBlank
    private String optionC;

    @NotBlank
    private String optionD;

    @NotBlank
    @Pattern(regexp = "A|B|C|D", message = "correctAnswer must be one of A, B, C, or D")
    private String correctAnswer;

    @NotNull
    @Min(value = 1, message = "marks must be at least 1")
    private Integer marks;

    @NotNull
    private Long testId;
}