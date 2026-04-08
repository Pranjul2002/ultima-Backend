package com.edutech.edutechbackend.dashboard.dto;

import com.edutech.edutechbackend.user.dto.UserProfileResponse;
import com.edutech.edutechbackend.user.dto.UserSettingsResponse;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private CurrentUserDto user;
    private OverviewDto overview;
    private UserProfileResponse profile;
    private List<TestItemDto> tests;
    private UserSettingsResponse settings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentUserDto {
        private String name;
        private String email;
        private String role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewDto {
        private List<StatDto> stats;
        private List<ProgressDto> progress;
        private List<ActivityDto> activity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatDto {
        private String label;
        private String value;
        private String sub;
        private String accent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressDto {
        private String label;
        private Integer value;
        private Integer max;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDto {
        private String title;
        private String time;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestItemDto {
        private Long id;
        private String title;
        private String subject;
        private String date;
        private Integer score;
        private Integer total;
        private String duration;
        private String status;
    }
}