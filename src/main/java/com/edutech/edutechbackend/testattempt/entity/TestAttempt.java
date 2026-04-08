package com.edutech.edutechbackend.testattempt.entity;

import com.edutech.edutechbackend.test.entity.Test;
import com.edutech.edutechbackend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "test_attempt",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "test_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // student who attempted
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // test attempted
    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer attemptedQuestions;

    @Column(nullable = false)
    private Integer correctAnswers;

    @Column(nullable = false)
    private Integer wrongAnswers;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer totalMarks;

    @Column(nullable = false)
    private LocalDateTime submittedAt;
}