package com.edutech.edutechbackend.upskilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SourceStatusResponse {
    private Long sourceId;
    private String fileName;
    private String status;
    private String summary;
    private Integer progress;
    private String error;
}