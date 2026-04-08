package com.edutech.edutechbackend.test.repository;


import com.edutech.edutechbackend.subject.entity.Subject;
import com.edutech.edutechbackend.test.entity.Test;
import com.edutech.edutechbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {

    // Free tests
    List<Test> findByIsPaidFalse();

    // Paid tests
    List<Test> findByIsPaidTrue();

    // Find paid tests that a specific mentor created
    List<Test> findByIsPaidTrueAndMentor(User mentor);

    // Find all tests by subject
    List<Test> findBySubject(Subject subject);
}
