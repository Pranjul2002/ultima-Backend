package com.edutech.edutechbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ↑ unique ID for each test paper
    //   DB generates this automatically

    @Column(nullable = false)
    private String title;
    // ↑ name of the test paper
    //   e.g. "Math Mid-Term 2026"
    //   admin types this when creating the test

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
    // ↑ which subject this test belongs to
    //
    //   @ManyToOne = many tests can belong to ONE subject
    //   e.g. Math can have 10 different test papers
    //
    //   @JoinColumn(name = "subject_id") = in the DB, this
    //   creates a column called "subject_id" in tests table
    //   that stores the ID of the subject
    //
    //   FetchType.LAZY = don't load Subject from DB
    //   until we actually access test.getSubject()
    //   saves unnecessary DB queries

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private Student createdBy;
    // ↑ which admin created this test
    //
    //   @ManyToOne = one admin can create MANY tests
    //   e.g. Admin Raj creates 5 test papers
    //
    //   stores admin's student ID in "created_by_id" column
    //   remember: admins are also stored in students table
    //   they just have role = ADMIN

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    // ↑ when was this test created
    //   stored automatically via @PrePersist below

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    // ↑ runs automatically just before saving to DB
    //   sets createdAt to current time
    //   we never need to set this manually
}