package com.edutech.edutechbackend.test.repository;

import com.edutech.edutechbackend.subject.entity.Subject;
import com.edutech.edutechbackend.test.entity.Test;
import com.edutech.edutechbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {

    /**
     * Fetch all free tests WITH their questions in a single JOIN query.
     *
     * Why: Test.questions is a @OneToMany with default LAZY fetch.
     * DashboardService calls test.getQuestions() to sum marks — if we use the
     * plain findByIsPaidFalse(), Hibernate fires N separate SELECT queries for
     * questions (one per test) AFTER the session may be closed, causing
     * LazyInitializationException → HTTP 500.
     *
     * JOIN FETCH loads everything in one query, eliminating the N+1 problem
     * and the session-closed error simultaneously.
     */
    @Query("SELECT DISTINCT t FROM Test t LEFT JOIN FETCH t.questions WHERE t.isPaid = false")
    List<Test> findFreeTestsWithQuestions();

    /**
     * Fetch all paid tests with questions eagerly (used by test listing endpoints).
     */
    @Query("SELECT DISTINCT t FROM Test t LEFT JOIN FETCH t.questions WHERE t.isPaid = true")
    List<Test> findPaidTestsWithQuestions();

    // ── Plain finders (safe to use where questions are NOT accessed) ──────────

    List<Test> findByIsPaidFalse();

    List<Test> findByIsPaidTrue();

    List<Test> findByIsPaidTrueAndMentor(User mentor);

    List<Test> findBySubject(Subject subject);
}