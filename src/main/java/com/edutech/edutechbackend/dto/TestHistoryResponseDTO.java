package com.edutech.edutechbackend.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@Builder
public class TestHistoryResponseDTO {

    private Long sessionId;
    // ↑ unique attempt id

    private String testTitle;
    // ↑ "Math Mid-Term 2026"

    private String subjectName;
    // ↑ "Math"

    private int score;
    // ↑ 7

    private int totalQuestions;
    // ↑ 10

    private double percentage;
    // ↑ 70.0

    private String takenAt;
    // ↑ "2026-03-21"
    //   shown in history list
}