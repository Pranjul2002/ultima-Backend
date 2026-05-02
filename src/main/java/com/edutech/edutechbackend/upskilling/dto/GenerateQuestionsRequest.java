package com.edutech.edutechbackend.upskilling.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateQuestionsRequest {

    @NotNull(message = "sourceId is required")
    private Long sourceId;

    // Number of questions to generate (default 5, max 15)
    @Min(1) @Max(15)
    private int count = 5;

    // Optional topic focus — e.g. "Chapter 3: Photosynthesis"
    private String topic;
}