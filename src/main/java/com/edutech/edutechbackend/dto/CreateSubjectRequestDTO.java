package com.edutech.edutechbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateSubjectRequestDTO {

    @NotBlank(message = "Subject name is required")
    private String name;
    // ↑ admin sends just the name
    //   e.g. { "name": "Math" }
    //   @NotBlank = can't be null, empty, or just spaces
}