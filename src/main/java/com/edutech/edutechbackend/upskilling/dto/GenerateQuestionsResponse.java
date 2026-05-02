package com.edutech.edutechbackend.upskilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQuestionsResponse {
    private Long sourceId;
    private String sourceFileName;
    private List<GeneratedQuestion> questions;
}