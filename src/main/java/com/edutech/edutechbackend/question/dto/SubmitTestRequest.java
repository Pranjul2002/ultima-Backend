package com.edutech.edutechbackend.question.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SubmitTestRequest {

    @NotEmpty(message = "answers cannot be empty")
    private List<StudentAnswerRequest> answers;
}