package com.edutech.edutechbackend.catalog.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookResponse {
    private String slug, classSlug, title, subtitle, description;
    private String subject, theme, emoji;
    private Integer chapterCount, questionCount;
}