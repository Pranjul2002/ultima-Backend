package com.edutech.edutechbackend.question.dto;

import com.edutech.edutechbackend.testattempt.dto.TestAttemptResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response for GET /api/tests/{testId}/review
 * Returns the user's last attempt stats + all questions with correct answers.
 */
@Data
@Builder
public class TestReviewResponse {

    private TestAttemptResponse        attempt;
    private List<ReviewQuestionResponse> questions;
}