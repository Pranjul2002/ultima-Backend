package com.edutech.edutechbackend.dto;

import com.edutech.edutechbackend.entity.AnswerOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateQuestionRequestDTO {

    @NotBlank(message = "Question text is required")
    private String questionText;
    // ↑ the actual question
    //   e.g. "What is 2+2?"

    @NotBlank(message = "Option A is required")
    private String optionA;
    // ↑ first choice e.g. "3"

    @NotBlank(message = "Option B is required")
    private String optionB;
    // ↑ second choice e.g. "4"

    @NotBlank(message = "Option C is required")
    private String optionC;
    // ↑ third choice e.g. "5"

    @NotBlank(message = "Option D is required")
    private String optionD;
    // ↑ fourth choice e.g. "6"

    @NotNull(message = "Correct option is required")
    private AnswerOption correctOption;
    // ↑ which option is correct: A, B, C, or D
    //   admin sends "B" → Jackson converts to AnswerOption.B
    //   stored in DB but NEVER sent to student
    //
    //   Full request example:
    //   {
    //     "questionText": "What is 2+2?",
    //     "optionA": "3",
    //     "optionB": "4",
    //     "optionC": "5",
    //     "optionD": "6",
    //     "correctOption": "B"
    //   }
}