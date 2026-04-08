package com.edutech.edutechbackend.user.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponse {

    private Notifications notifications;
    private Privacy privacy;
    private Security security;
    private Preferences preferences;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notifications {
        private boolean emailReminders;
        private boolean testAlerts;
        private boolean progressReports;
        private boolean newCourses;
        private boolean badges;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Privacy {
        private boolean publicProfile;
        private boolean showProgress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Security {
        private boolean twoFactor;
        private boolean loginAlerts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Preferences {
        private String language;
        private String timezone;
        private Integer dailyGoal;
        private String recoveryEmail;
    }
}