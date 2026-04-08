package com.edutech.edutechbackend.user.controller;

import com.edutech.edutechbackend.user.dto.UpdateProfileRequest;
import com.edutech.edutechbackend.user.dto.UpdateUserSettingsRequest;
import com.edutech.edutechbackend.user.dto.UserProfileResponse;
import com.edutech.edutechbackend.user.dto.UserSettingsResponse;
import com.edutech.edutechbackend.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userProfileService.getProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userProfileService.updateProfile(request));
    }

    @GetMapping("/settings")
    public ResponseEntity<UserSettingsResponse> getSettings() {
        return ResponseEntity.ok(userProfileService.getSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<UserSettingsResponse> updateSettings(@RequestBody UpdateUserSettingsRequest request) {
        return ResponseEntity.ok(userProfileService.updateSettings(request));
    }
}