package com.edutech.edutechbackend.testattempt.repository;

import com.edutech.edutechbackend.test.entity.Test;
import com.edutech.edutechbackend.testattempt.entity.TestAttempt;
import com.edutech.edutechbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    Optional<TestAttempt> findByStudentAndTest(User student, Test test);

    List<TestAttempt> findByStudentOrderBySubmittedAtDesc(User student);

    void deleteByStudentAndTest(User student, Test test);

    /** Used by DataInitializer to wipe attempts before deleting old seed tests. */
    void deleteByTest(Test test);
}
