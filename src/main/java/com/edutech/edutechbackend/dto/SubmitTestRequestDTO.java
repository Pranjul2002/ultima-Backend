package com.edutech.edutechbackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class SubmitTestRequestDTO {

    @NotNull(message = "Test ID is required")
    private Long testId;
    // ↑ which test paper student is submitting
    //   e.g. testId: 1 = "Math Mid-Term 2026"

    @NotEmpty(message = "Answers cannot be empty")
    private List<AnswerDTO> answers;
    // ↑ list of all answers student gave
    //   one AnswerDTO per question
    //
    //   Full request example:
    //   {
    //     "testId": 1,
    //     "answers": [
    //       { "questionId": 1, "selectedOption": "B" },
    //       { "questionId": 2, "selectedOption": "A" },
    //       { "questionId": 3, "selectedOption": "C" }
    //     ]
    //   }

    @Getter @Setter
    public static class AnswerDTO {
        // ↑ static inner class
        //   lives inside SubmitTestRequestDTO
        //   represents ONE answer for ONE question
        //   keeps related things together in one file

        @NotNull(message = "Question ID is required")
        private Long questionId;
        // ↑ which question this answer is for

        @NotNull(message = "Selected option is required")
        private com.edutech.edutechbackend.entity.AnswerOption selectedOption;
        // ↑ what the student picked: A, B, C, or D
    }
}