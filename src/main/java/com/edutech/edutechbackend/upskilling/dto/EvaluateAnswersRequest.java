package com.edutech.edutechbackend.upskilling.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EvaluateAnswersRequest {

    @NotNull(message = "sourceId is required")
    private Long sourceId;

    @NotEmpty(message = "answers list cannot be empty")
    private List<StudentAnswer> answers;

    @Data
    public static class StudentAnswer {
        private int questionNumber;
        private String questionText;

        // One of these will be set — text answer OR base64-encoded PDF/image
        private String textAnswer;
        private String handwrittenBase64;  // base64 of PDF or image
        private String handwrittenMimeType; // "application/pdf" | "image/jpeg" | "image/png"
    }
}