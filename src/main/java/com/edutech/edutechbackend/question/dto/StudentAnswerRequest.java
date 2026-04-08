package com.edutech.edutechbackend.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StudentAnswerRequest {

    @NotNull
    private Long questionId;

    @NotBlank
    @Pattern(regexp = "A|B|C|D", message = "selectedAnswer must be A, B, C, or D")
    private String selectedAnswer;
}