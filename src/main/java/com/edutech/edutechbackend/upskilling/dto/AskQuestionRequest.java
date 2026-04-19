package com.edutech.edutechbackend.upskilling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AskQuestionRequest {

    @NotNull(message = "sourceId is required")
    private Long sourceId;

    @NotBlank(message = "question is required")
    private String question;

    private Boolean strictContext = true;
}