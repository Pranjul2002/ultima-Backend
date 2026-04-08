package com.edutech.edutechbackend.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRegisterRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String username;

    @NotNull
    @Past
    private LocalDate dateOfBirth;

    @NotBlank
    private String role;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}