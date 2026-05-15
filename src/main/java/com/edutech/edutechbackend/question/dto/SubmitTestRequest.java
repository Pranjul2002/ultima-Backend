package com.edutech.edutechbackend.question.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubmitTestRequest {

    // @NotNull (not @NotEmpty) — an empty list is valid when a student
    // skips all questions. The service will score it as 0 correctly.
    @NotNull(message = "answers must not be null")
    private List<StudentAnswerRequest> answers = new ArrayList<>();
}