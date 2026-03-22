package com.edutech.edutechbackend.repository;

import com.edutech.edutechbackend.entity.Test;
import com.edutech.edutechbackend.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {

    List<Test> findBySubjectId(Long subjectId);
    // ↑ Spring builds:
    //   SELECT * FROM tests WHERE subject_id = ?
    //
    //   Used when student clicks "Math" subject
    //   returns ALL test papers under Math
    //   e.g. [Mid-Term, Final Exam, Practice Set]

    List<Test> findByCreatedBy(Student admin);
    // ↑ Spring builds:
    //   SELECT * FROM tests WHERE created_by_id = ?
    //
    //   Used when admin wants to see only THEIR test papers
    //   Admin Raj sees only his tests
    //   Admin Priya sees only her tests
    //   they don't see each other's tests in admin panel

    boolean existsByTitleAndSubjectId(String title, Long subjectId);
    // ↑ Spring builds:
    //   SELECT COUNT(*) FROM tests
    //   WHERE title = ? AND subject_id = ?
    //
    //   prevents duplicate: same admin can't create
    //   two tests with same name in same subject
    //   e.g. can't have two "Math Mid-Term 2026" in Math
}