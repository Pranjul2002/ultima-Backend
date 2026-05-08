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
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository         userRepository;
    private final UserSettingsRepository userSettingsRepository;

    /**
     * Returns a FRESH copy of the current user loaded from the DB within
     * the active transaction.
     *
     * Why not just cast the SecurityContext principal?
     *   The User stored in the SecurityContext was fetched during JWT filter
     *   execution (a different, already-closed Hibernate session). Using that
     *   detached entity and accessing its lazy collections (subjects, settings)
     *   causes LazyInitializationException → HTTP 500.
     *
     *   Re-fetching by ID keeps everything in the current transaction's session.
     */
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User user)) {
            throw new RuntimeException("Unauthorized");
        }

        // Re-fetch from DB — never use the detached principal for lazy associations
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile() {
        return mapProfile(getCurrentUser());
    }

    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        if (request.getName() != null)     user.setUsername(request.getName().trim());
        if (request.getEmail() != null)    user.setEmail(request.getEmail().trim().toLowerCase());
        if (request.getBio() != null)      user.setBio(request.getBio());
        if (request.getLocation() != null) user.setLocation(request.getLocation());
        if (request.getWebsite() != null)  user.setWebsite(request.getWebsite());

        userRepository.save(user);
        return mapProfile(user);
    }

    @Transactional(readOnly = true)
    public UserSettingsResponse getSettings() {
        User user = getCurrentUser();
        UserSettings settings = getOrCreateSettings(user);
        return mapSettings(settings);
    }

    @Transactional
    public UserSettingsResponse updateSettings(UpdateUserSettingsRequest request) {
        User user = getCurrentUser();
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

    // ── private helpers ───────────────────────────────────────────────────────

    private UserSettings getOrCreateSettings(User user) {
        return userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> userSettingsRepository.save(
                        UserSettings.builder()
                                .user(user)
                                .emailReminders(false).testAlerts(false)
                                .progressReports(false).newCourses(false).badges(false)
                                .publicProfile(false).showProgress(false)
                                .twoFactor(false).loginAlerts(false)
                                .language("en").timezone("Asia/Kolkata")
                                .dailyGoal(30).recoveryEmail("")
                                .build()
                ));
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
                        .courses(0).tests(0).badges(0)
                        .build())
                .build();
    }

    private UserSettingsResponse mapSettings(UserSettings s) {
        return UserSettingsResponse.builder()
                .notifications(UserSettingsResponse.Notifications.builder()
                        .emailReminders(s.isEmailReminders()).testAlerts(s.isTestAlerts())
                        .progressReports(s.isProgressReports()).newCourses(s.isNewCourses())
                        .badges(s.isBadges()).build())
                .privacy(UserSettingsResponse.Privacy.builder()
                        .publicProfile(s.isPublicProfile()).showProgress(s.isShowProgress()).build())
                .security(UserSettingsResponse.Security.builder()
                        .twoFactor(s.isTwoFactor()).loginAlerts(s.isLoginAlerts()).build())
                .preferences(UserSettingsResponse.Preferences.builder()
                        .language(s.getLanguage()).timezone(s.getTimezone())
                        .dailyGoal(s.getDailyGoal()).recoveryEmail(s.getRecoveryEmail()).build())
                .build();
    }
}