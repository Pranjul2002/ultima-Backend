package com.edutech.edutechbackend.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@Builder
public class TestResultResponseDTO {

    private Long sessionId;
    // ↑ unique ID of this attempt
    //   student can use this later to review answers

    private String testTitle;
    // ↑ "Math Mid-Term 2026"
    //   shown in result screen

    private int score;
    // ↑ how many correct e.g. 7

    private int totalQuestions;
    // ↑ total questions e.g. 10

    private double percentage;
    // ↑ 70.0

    private String takenAt;
    // ↑ when they submitted e.g. "2026-03-21 14:30:00"

    private List<AnswerResultDTO> answerResults;
    // ↑ detailed breakdown of each question
    //   student sees which ones they got right/wrong
    //   and what the correct answer was

    @Getter @Setter
    @AllArgsConstructor
    @Builder
    public static class AnswerResultDTO {
        // ↑ one entry per question in the result

        private Long questionId;
        private String questionText;
        // ↑ "What is 2+2?"

        private String selectedOption;
        // ↑ what student picked e.g. "A"

        private String correctOption;
        // ↑ what the correct answer was e.g. "B"
        //   NOW we reveal it — test is already submitted
        //   showing correct answers AFTER submission is fine

        private boolean correct;
        // ↑ true if selectedOption == correctOption
    }
}