package com.edutech.edutechbackend.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@Builder
public class QuestionResponseDTO {

    private Long id;
    // ↑ student needs this when submitting answers
    //   { "questionId": 1, "selectedOption": "B" }

    private String questionText;
    // ↑ "What is 2+2?"

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    // ↑ all 4 choices shown to student

    // ⚠️ NO correctOption field here!
    // ↑ if we included it → student sees the answer
    //   correctOption stays on server side only
    //   only used during scoring in TestService
}