package com.edutech.edutechbackend.repository;

import com.edutech.edutechbackend.entity.TestAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestAnswerRepository extends JpaRepository<TestAnswer, Long> {

    List<TestAnswer> findBySessionId(Long sessionId);
    // ↑ Spring builds:
    //   SELECT * FROM test_answers WHERE session_id = ?
    //
    //   Used when student wants to review their attempt
    //   returns every answer they gave in that session
    //   e.g. Q1→B(correct), Q2→A(wrong), Q3→C(correct)
}