package com.edutech.edutechbackend.controller;

import com.edutech.edutechbackend.dto.CreateSubjectRequestDTO;
import com.edutech.edutechbackend.dto.SubjectResponseDTO;
import com.edutech.edutechbackend.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
// ↑ marks this as a REST API controller
//   every method returns JSON automatically
//   no need for @ResponseBody on each method

@RequestMapping("/api/admin/subjects")
// ↑ all endpoints in this controller
//   start with /api/admin/subjects
//   SecurityConfig already protects /api/admin/**
//   with hasRole("ADMIN") — so only admins reach here

@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    // ── POST /api/admin/subjects ──────────────────────────────────────────
    @PostMapping
    public ResponseEntity<SubjectResponseDTO> createSubject(
            @Valid @RequestBody CreateSubjectRequestDTO request) {
        // ↑ @Valid triggers validation annotations
        //   in CreateSubjectRequestDTO (@NotBlank etc)
        //   if validation fails → 400 Bad Request automatically
        //
        // @RequestBody converts incoming JSON to DTO object:
        // { "name": "Math" } → CreateSubjectRequestDTO

        SubjectResponseDTO response = subjectService.createSubject(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // ↑ 201 Created — standard for successful resource creation
    }

    // ── GET /api/admin/subjects ───────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<SubjectResponseDTO>> getAllSubjects() {

        List<SubjectResponseDTO> subjects = subjectService.getAllSubjects();

        return ResponseEntity.ok(subjects);
        // ↑ 200 OK with list of all subjects
    }
}