package com.edutech.edutechbackend.user.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String email;
    private String bio;
    private String location;
    private String website;
}