package com.edutech.edutechbackend.test.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class TestCreateRequest {

    @NotBlank
    private String title;

    @NotNull
    private Boolean isPaid;

    private Double price; // can be null if free

    @NotBlank
    private String subjectName; // subject linked to test
}
