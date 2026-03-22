package com.edutech.edutechbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_answers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TestAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private TestSession session;
    // ↑ which attempt this answer belongs to
    //   groups all answers of one attempt together

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    // ↑ which question was answered

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnswerOption selectedOption;
    // ↑ what the student picked: A, B, C, or D

    @Column(nullable = false)
    private boolean correct;
    // ↑ was their answer correct?
    //   true = selectedOption matches question.correctOption
    //   false = wrong answer
    //   calculated at submission time
}