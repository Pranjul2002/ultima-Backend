package com.edutech.edutechbackend.user.service;

import com.edutech.edutechbackend.user.dto.UpdateProfileRequest;
import com.edutech.edutechbackend.user.dto.UpdateUserSettingsRequest;
import com.edutech.edutechbackend.user.dto.UserProfileResponse;
import com.edutech.edutechbackend.user.dto.UserSettingsResponse;
import com.edutech.edutechbackend.user.entity.User;
import com.edutech.edutechbackend.user.entity.UserSettings;
import com.edutech.edutechbackend.user.repository.UserRepository;
import com.edutech.edutechbackend.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User user)) {
            throw new RuntimeException("Unauthorized");
        }

        return user;
    }

    public UserProfileResponse getProfile() {
        User user = getCurrentUser();
        return mapProfile(user);
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = userRepository.findById(getCurrentUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) {
            user.setUsername(request.getName().trim());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail().trim().toLowerCase());
        }

        user.setBio(request.getBio());
        user.setLocation(request.getLocation());
        user.setWebsite(request.getWebsite());

        userRepository.save(user);
        return mapProfile(user);
    }

    public UserSettingsResponse getSettings() {
        User user = getCurrentUser();
        UserSettings settings = getOrCreateSettings(user);
        return mapSettings(settings);
    }

    public UserSettingsResponse updateSettings(UpdateUserSettingsRequest request) {
        User user = userRepository.findById(getCurrentUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserSettings settings = getOrCreateSettings(user);

        if (request.getNotifications() != null) {
            settings.setEmailReminders(request.getNotifications().isEmailReminders());
            settings.setTestAlerts(request.getNotifications().isTestAlerts());
            settings.setProgressReports(request.getNotifications().isProgressReports());
            settings.setNewCourses(request.getNotifications().isNewCourses());
            settings.setBadges(request.getNotifications().isBadges());
        }

        if (request.getPrivacy() != null) {
            settings.setPublicProfile(request.getPrivacy().isPublicProfile());
            settings.setShowProgress(request.getPrivacy().isShowProgress());
        }

        if (request.getSecurity() != null) {
            settings.setTwoFactor(request.getSecurity().isTwoFactor());
            settings.setLoginAlerts(request.getSecurity().isLoginAlerts());
        }

        if (request.getPreferences() != null) {
            settings.setLanguage(request.getPreferences().getLanguage());
            settings.setTimezone(request.getPreferences().getTimezone());
            settings.setDailyGoal(request.getPreferences().getDailyGoal());
            settings.setRecoveryEmail(request.getPreferences().getRecoveryEmail());
        }

        userSettingsRepository.save(settings);
        return mapSettings(settings);
    }

    private UserSettings getOrCreateSettings(User user) {
        return userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserSettings settings = UserSettings.builder()
                            .user(user)
                            .emailReminders(false)
                            .testAlerts(false)
                            .progressReports(false)
                            .newCourses(false)
                            .badges(false)
                            .publicProfile(false)
                            .showProgress(false)
                            .twoFactor(false)
                            .loginAlerts(false)
                            .language("en")
                            .timezone("Asia/Kolkata")
                            .dailyGoal(30)
                            .recoveryEmail("")
                            .build();

                    return userSettingsRepository.save(settings);
                });
    }

    private UserProfileResponse mapProfile(User user) {
        return UserProfileResponse.builder()
                .name(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio() == null ? "" : user.getBio())
                .location(user.getLocation() == null ? "" : user.getLocation())
                .website(user.getWebsite() == null ? "" : user.getWebsite())
                .role(user.getRole().name())
                .avatarUrl("")
                .skills(Collections.emptyList())
                .certificates(Collections.emptyList())
                .metrics(UserProfileResponse.MetricsDto.builder()
                        .courses(0)
                        .tests(0)
                        .badges(0)
                        .build())
                .build();
    }

    private UserSettingsResponse mapSettings(UserSettings settings) {
        return UserSettingsResponse.builder()
                .notifications(UserSettingsResponse.Notifications.builder()
                        .emailReminders(settings.isEmailReminders())
                        .testAlerts(settings.isTestAlerts())
                        .progressReports(settings.isProgressReports())
                        .newCourses(settings.isNewCourses())
                        .badges(settings.isBadges())
                        .build())
                .privacy(UserSettingsResponse.Privacy.builder()
                        .publicProfile(settings.isPublicProfile())
                        .showProgress(settings.isShowProgress())
                        .build())
                .security(UserSettingsResponse.Security.builder()
                        .twoFactor(settings.isTwoFactor())
                        .loginAlerts(settings.isLoginAlerts())
                        .build())
                .preferences(UserSettingsResponse.Preferences.builder()
                        .language(settings.getLanguage())
                        .timezone(settings.getTimezone())
                        .dailyGoal(settings.getDailyGoal())
                        .recoveryEmail(settings.getRecoveryEmail())
                        .build())
                .build();
    }
}