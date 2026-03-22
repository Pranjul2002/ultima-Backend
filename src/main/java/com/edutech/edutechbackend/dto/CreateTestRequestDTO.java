package com.edutech.edutechbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateTestRequestDTO {

    @NotBlank(message = "Test title is required")
    private String title;
    // ↑ name of the test paper
    //   e.g. "Math Mid-Term 2026"

    @NotNull(message = "Subject ID is required")
    private Long subjectId;
    // ↑ which subject this test belongs to
    //   admin sends the subject's id
    //   e.g. { "title": "Math Mid-Term 2026", "subjectId": 1 }
    //
    //   we use this id to find the Subject from DB
    //   and link it to the Test entity
}