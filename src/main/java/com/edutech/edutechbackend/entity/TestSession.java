package com.edutech.edutechbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_sessions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TestSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    // ↑ which student attempted this test
    //   one student can have MANY sessions (retakes allowed)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;
    // ↑ which test paper was attempted
    //   one test can be attempted by MANY students

    @Column(nullable = false)
    private int score;
    // ↑ how many questions answered correctly
    //   e.g. 7 (out of 10)

    @Column(nullable = false)
    private int totalQuestions;
    // ↑ total questions in the test
    //   e.g. 10
    //   stored here so even if questions are edited later
    //   the original count is preserved

    @Column(nullable = false)
    private double percentage;
    // ↑ score / totalQuestions * 100
    //   e.g. 7/10 * 100 = 70.0
    //   calculated and stored at submission time

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @PrePersist
    public void prePersist() {
        this.takenAt = LocalDateTime.now();
    }
}