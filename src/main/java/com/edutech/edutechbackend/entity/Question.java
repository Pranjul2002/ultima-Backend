package com.edutech.edutechbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;
    // ↑ which test paper this question belongs to
    //
    //   @ManyToOne = one test can have MANY questions
    //   e.g. "Math Mid-Term" has 10 questions
    //
    //   stores test's ID in "test_id" column
    //   this is how questions stay grouped per test
    //   questions from different tests NEVER mix

    @Column(nullable = false, length = 1000)
    private String questionText;
    // ↑ the actual question
    //   e.g. "What is the value of π (pi) rounded to 2 decimal places?"
    //   length = 1000 allows longer questions

    @Column(nullable = false)
    private String optionA;
    // ↑ first choice, e.g. "3.14"

    @Column(nullable = false)
    private String optionB;
    // ↑ second choice, e.g. "3.16"

    @Column(nullable = false)
    private String optionC;
    // ↑ third choice, e.g. "3.12"

    @Column(nullable = false)
    private String optionD;
    // ↑ fourth choice, e.g. "3.41"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnswerOption correctOption;
    // ↑ which option is correct: A, B, C, or D
    //
    //   @Enumerated(EnumType.STRING) = store as "A", "B" etc
    //   NOT as 0, 1, 2, 3 (those are harder to read in DB)
    //
    //   THIS IS NEVER SENT TO STUDENT in the questions response
    //   only used server-side when scoring the submission
    //   if we sent this → student sees answers before submitting!
}