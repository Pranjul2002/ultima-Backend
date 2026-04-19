package com.edutech.edutechbackend.upskilling.controller;

import com.edutech.edutechbackend.upskilling.dto.AskQuestionRequest;
import com.edutech.edutechbackend.upskilling.dto.AskQuestionResponse;
import com.edutech.edutechbackend.upskilling.service.UpskillingChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class UpskillingChatController {

    private final UpskillingChatService upskillingChatService;

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@Valid @RequestBody AskQuestionRequest request) {
        try {
            AskQuestionResponse response = upskillingChatService.askQuestion(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", ex.getMessage()
            ));
        }
    }
}