package com.edutech.edutechbackend.catalog.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterResponse {

    private String  chapterId;
    private String  bookSlug;
    private Integer number;
    private String  title;
    private String  description;
    private String  duration;
    private Integer questionCount;
    private Boolean comingSoon;

    /**
     * The backend Test ID linked to this chapter.
     * Frontend uses this to call /api/tests/{linkedTestId}/questions.
     * Null when no test has been linked yet.
     */
    private Long linkedTestId;
}