package com.edutech.edutechbackend.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@Builder
public class TestResponseDTO {

    private Long id;
    // ↑ student needs this to fetch questions
    //   e.g. GET /api/tests/papers/1/questions

    private String title;
    // ↑ "Math Mid-Term 2026"
    //   displayed in test paper list

    private String subjectName;
    // ↑ "Math"
    //   so frontend doesn't need a separate call
    //   to find subject name from subject id

    private String createdByName;
    // ↑ "Admin Raj"
    //   student can see which teacher made this test
    //   helps distinguish between two Math tests
    //   "Math Mid-Term by Raj" vs "Math Practice by Priya"

    private int totalQuestions;
    // ↑ how many questions are in this test
    //   shown before student starts
    //   e.g. "10 questions"

    private String createdAt;
    // ↑ when the test was created
    //   e.g. "2026-01-10"
}