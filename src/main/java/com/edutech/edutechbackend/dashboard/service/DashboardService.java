package com.edutech.edutechbackend.dashboard.service;

import com.edutech.edutechbackend.dashboard.dto.DashboardResponse;
import com.edutech.edutechbackend.user.entity.User;
import com.edutech.edutechbackend.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserProfileService userProfileService;

    public DashboardResponse getDashboard() {
        User user = userProfileService.getCurrentUser();

        return DashboardResponse.builder()
                .user(DashboardResponse.CurrentUserDto.builder()
                        .name(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .overview(DashboardResponse.OverviewDto.builder()
                        .stats(Collections.emptyList())
                        .progress(Collections.emptyList())
                        .activity(Collections.emptyList())
                        .build())
                .profile(userProfileService.getProfile())
                .tests(Collections.emptyList())
                .settings(userProfileService.getSettings())
                .build();
    }
}