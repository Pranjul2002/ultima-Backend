package com.edutech.edutechbackend.testattempt.repository;

import com.edutech.edutechbackend.test.entity.Test;
import com.edutech.edutechbackend.testattempt.entity.TestAttempt;
import com.edutech.edutechbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    Optional<TestAttempt> findByStudentAndTest(User student, Test test);

    List<TestAttempt> findByStudentOrderBySubmittedAtDesc(User student);

    // @Modifying + JPQL ensures the DELETE is executed immediately (flushed)
    // before the INSERT, preventing the unique-constraint violation on (student_id, test_id).
    @Modifying
    @Query("DELETE FROM TestAttempt ta WHERE ta.student = :student AND ta.test = :test")
    void deleteByStudentAndTest(@Param("student") User student, @Param("test") Test test);

    /** Used by DataInitializer to wipe attempts before deleting old seed tests. */
    @Modifying
    @Query("DELETE FROM TestAttempt ta WHERE ta.test = :test")
    void deleteByTest(@Param("test") Test test);
}