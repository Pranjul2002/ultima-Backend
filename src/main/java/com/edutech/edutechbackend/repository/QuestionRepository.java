package com.edutech.edutechbackend.repository;

import com.edutech.edutechbackend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTestId(Long testId);
    // ↑ Spring builds:
    //   SELECT * FROM questions WHERE test_id = ?
    //
    //   Used when student opens a test paper
    //   returns ALL questions for that specific test
    //   e.g. all 10 questions of "Math Mid-Term 2026"
    //
    //   IMPORTANT: correctOption is in these Question objects
    //   but we will HIDE it before sending to student
    //   we only expose it server-side during scoring

    int countByTestId(Long testId);
    // ↑ Spring builds:
    //   SELECT COUNT(*) FROM questions WHERE test_id = ?
    //
    //   Used when showing test paper list to student
    //   so student can see "Math Mid-Term (10 questions)"
    //   before deciding which test to take
}