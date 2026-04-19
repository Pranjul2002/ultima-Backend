package com.edutech.edutechbackend.user.controller;

import com.edutech.edutechbackend.user.dto.BookDto;
import com.edutech.edutechbackend.user.dto.UpdateProfileRequest;
import com.edutech.edutechbackend.user.dto.UpdateUserSettingsRequest;
import com.edutech.edutechbackend.user.dto.UserProfileResponse;
import com.edutech.edutechbackend.user.dto.UserSettingsResponse;
import com.edutech.edutechbackend.user.service.UserBookService;
import com.edutech.edutechbackend.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;
    private final UserBookService    userBookService;   // ← NEW

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

    /**
     * GET /api/user/my-books
     *
     * Returns the full book library for the logged-in user:
     *   - Free NCERT books (Physics, Chemistry, Maths — classes 10/11/12) for everyone
     *   - Any additional books the user has purchased
     *
     * JWT required (handled by SecurityConfig — all /api/user/** routes are authenticated).
     */
    @GetMapping("/my-books")
    public ResponseEntity<List<BookDto>> getMyBooks() {
        return ResponseEntity.ok(userBookService.getMyBooks());
    }
}
