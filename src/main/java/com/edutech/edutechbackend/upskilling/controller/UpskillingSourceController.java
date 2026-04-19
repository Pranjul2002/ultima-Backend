package com.edutech.edutechbackend.upskilling.controller;

import com.edutech.edutechbackend.upskilling.dto.SourceStatusResponse;
import com.edutech.edutechbackend.upskilling.dto.SourceUploadResponse;
import com.edutech.edutechbackend.upskilling.service.UpskillingSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class UpskillingSourceController {

    private final UpskillingSourceService upskillingSourceService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadSource(@RequestParam("file") MultipartFile file) {
        try {
            SourceUploadResponse response = upskillingSourceService.uploadSource(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", ex.getMessage()
            ));
        }
    }

    @GetMapping("/{sourceId}/status")
    public ResponseEntity<?> getSourceStatus(@PathVariable Long sourceId) {
        try {
            SourceStatusResponse response = upskillingSourceService.getSourceStatus(sourceId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", ex.getMessage()
            ));
        }
    }
}