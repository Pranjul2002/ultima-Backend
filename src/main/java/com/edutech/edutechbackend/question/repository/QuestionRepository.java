package com.edutech.edutechbackend.question.repository;

import com.edutech.edutechbackend.question.entity.Question;
import com.edutech.edutechbackend.test.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTest(Test test);

    List<Question> findByTestId(Long testId);

    long countByTestId(Long testId);
}