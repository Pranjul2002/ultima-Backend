package com.edutech.edutechbackend.upskilling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AskQuestionRequest {

    @NotNull(message = "sourceId is required")
    private Long sourceId;

    @NotBlank(message = "question is required")
    private String question;

    private Boolean strictContext = true;

    // Each item: { "role": "user" | "assistant", "content": "..." }
    // Frontend sends the full chat history so Groq has context
    private List<HistoryMessage> history = new ArrayList<>();

    @Data
    public static class HistoryMessage {
        private String role;    // "user" or "assistant"
        private String content;
    }
}