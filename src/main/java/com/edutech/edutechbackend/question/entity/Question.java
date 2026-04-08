package com.edutech.edutechbackend.question.entity;

import com.edutech.edutechbackend.test.entity.Test;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String questionText;

    @Column(nullable = false)
    private String optionA;

    @Column(nullable = false)
    private String optionB;

    @Column(nullable = false)
    private String optionC;

    @Column(nullable = false)
    private String optionD;

    @Column(nullable = false)
    private String correctAnswer; // A, B, C, or D

    @Column(nullable = false)
    private Integer marks;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;
}