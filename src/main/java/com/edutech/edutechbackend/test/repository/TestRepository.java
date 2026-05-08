package com.edutech.edutechbackend.test.repository;

import com.edutech.edutechbackend.subject.entity.Subject;
import com.edutech.edutechbackend.test.entity.Test;
import com.edutech.edutechbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {

    /**
     * Loads free tests + their questions + subject in ONE SQL query via JOIN FETCH.
     *
     * Without this, DashboardService calling test.getQuestions() fires N separate
     * lazy SELECTs after the Hibernate session is closed → LazyInitializationException → 500.
     *
     * DISTINCT prevents duplicate Test rows caused by the one-to-many join.
     */
    @Query("""
        SELECT DISTINCT t FROM Test t
        LEFT JOIN FETCH t.questions
        LEFT JOIN FETCH t.subject
        WHERE t.isPaid = false
        """)
    List<Test> findFreeTestsWithQuestions();

    @Query("""
        SELECT DISTINCT t FROM Test t
        LEFT JOIN FETCH t.questions
        LEFT JOIN FETCH t.subject
        WHERE t.isPaid = true
        """)
    List<Test> findPaidTestsWithQuestions();

    // Plain finders — safe to use where questions/subject are NOT accessed
    List<Test> findByIsPaidFalse();
    List<Test> findByIsPaidTrue();
    List<Test> findByIsPaidTrueAndMentor(User mentor);
    List<Test> findBySubject(Subject subject);
}