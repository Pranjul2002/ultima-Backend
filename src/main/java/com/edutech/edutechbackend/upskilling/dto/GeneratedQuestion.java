package com.edutech.edutechbackend.upskilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single AI-generated question, sent to the frontend for the student to answer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedQuestion {

    private int number;        // 1-based index
    private String question;   // The question text
    private String type;       // "short_answer" | "descriptive" (for future expansion)
}