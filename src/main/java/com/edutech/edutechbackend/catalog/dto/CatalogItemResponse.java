package com.edutech.edutechbackend.catalog.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CatalogItemResponse {
    private String slug, title, subtitle, description, icon, category, href;
    private StatsDto stats;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StatsDto {
        private Integer subjects;
        private String questions;
        private Integer tests;
    }
}