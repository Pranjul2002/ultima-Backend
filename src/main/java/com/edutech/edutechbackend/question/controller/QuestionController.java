package com.edutech.edutechbackend.question.controller;

import com.edutech.edutechbackend.question.dto.QuestionCreateRequest;
import com.edutech.edutechbackend.question.entity.Question;
import com.edutech.edutechbackend.question.service.QuestionService;
import com.edutech.edutechbackend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/create")
    public Question createQuestion(@Valid @RequestBody QuestionCreateRequest request) {

        User currentUser = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return questionService.createQuestion(currentUser, request);
    }
}