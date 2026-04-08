package com.edutech.edutechbackend.test.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestResponse {

    private Long id;
    private String title;
    private boolean isPaid;
    private Double price;
    private Long subjectId;
    private String subjectName;
    private Long mentorId;
    private String mentorName;
}