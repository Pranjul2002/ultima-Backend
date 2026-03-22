package com.edutech.edutechbackend.repository;

import com.edutech.edutechbackend.entity.TestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestSessionRepository extends JpaRepository<TestSession, Long> {

    List<TestSession> findByStudentId(Long studentId);
    // ↑ Spring builds:
    //   SELECT * FROM test_sessions WHERE student_id = ?
    //
    //   Used for student's test history page
    //   returns all attempts by this student
    //   e.g. Aryan's 3 attempts across different tests

    List<TestSession> findByStudentIdOrderByTakenAtDesc(Long studentId);
    // ↑ Spring builds:
    //   SELECT * FROM test_sessions
    //   WHERE student_id = ?
    //   ORDER BY taken_at DESC
    //
    //   Same as above but SORTED by date
    //   newest attempt appears first in history
    //   much better UX for the student

    int countByStudentId(Long studentId);
    // ↑ Spring builds:
    //   SELECT COUNT(*) FROM test_sessions WHERE student_id = ?
    //
    //   Used for student profile:
    //   totalTestsTaken = countByStudentId(studentId)

    @Query("SELECT AVG(ts.percentage) FROM TestSession ts WHERE ts.student.id = :studentId")
        // ↑ this is JPQL (Java Persistence Query Language)
        //   similar to SQL but uses Java class names not table names
        //   "TestSession" = the Java class (not test_sessions table)
        //   "ts.student.id" = navigate through the relationship
        //
        //   Actual SQL Spring generates:
        //   SELECT AVG(percentage) FROM test_sessions WHERE student_id = ?
    Double findAverageScoreByStudentId(@Param("studentId") Long studentId);
    // ↑ Used for student profile:
    //   averageScore = findAverageScoreByStudentId(studentId)
    //   returns null if student has no attempts yet
    //   → we handle that by defaulting to 0.0
}