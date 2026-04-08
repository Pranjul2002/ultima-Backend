package com.edutech.edutechbackend.user.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String name;
    private String email;
    private String bio;
    private String location;
    private String website;
    private String role;
    private String avatarUrl;
    private List<SkillDto> skills;
    private List<CertificateDto> certificates;
    private MetricsDto metrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillDto {
        private String label;
        private String level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateDto {
        private String title;
        private String issuer;
        private String date;
        private String icon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricsDto {
        private Integer courses;
        private Integer tests;
        private Integer badges;
    }
}