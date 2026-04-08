package com.edutech.edutechbackend.user.dto;

import lombok.Data;

@Data
public class UpdateUserSettingsRequest {

    private Notifications notifications;
    private Privacy privacy;
    private Security security;
    private Preferences preferences;

    @Data
    public static class Notifications {
        private boolean emailReminders;
        private boolean testAlerts;
        private boolean progressReports;
        private boolean newCourses;
        private boolean badges;
    }

    @Data
    public static class Privacy {
        private boolean publicProfile;
        private boolean showProgress;
    }

    @Data
    public static class Security {
        private boolean twoFactor;
        private boolean loginAlerts;
    }

    @Data
    public static class Preferences {
        private String language;
        private String timezone;
        private Integer dailyGoal;
        private String recoveryEmail;
    }
}