package com.edutech.edutechbackend.question.service;

import com.edutech.edutechbackend.question.dto.QuestionCreateRequest;
import com.edutech.edutechbackend.question.entity.Question;
import com.edutech.edutechbackend.question.repository.QuestionRepository;
import com.edutech.edutechbackend.test.entity.Test;
import com.edutech.edutechbackend.test.repository.TestRepository;
import com.edutech.edutechbackend.user.entity.User;
import com.edutech.edutechbackend.user.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TestRepository testRepository;

    public Question createQuestion(User currentUser, QuestionCreateRequest request) {

        // 1. Check user role
        if (currentUser.getRole() != Role.MENTOR) {
            throw new RuntimeException("Only mentors can create questions");
        }

        // 2. Find test
        Test test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new RuntimeException("Test not found"));

        // 3. Check test ownership
        if (!test.getMentor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only add questions to your own tests");
        }

        // 4. Normalize correct answer
        String correctAnswer = request.getCorrectAnswer().toUpperCase();

        // 5. Extra validation (defensive layer)
        if (!List.of("A", "B", "C", "D").contains(correctAnswer)) {
            throw new RuntimeException("Correct answer must be one of A, B, C, or D");
        }

        // 6. Create question
        Question question = Question.builder()
                .questionText(request.getQuestionText())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .correctAnswer(correctAnswer)
                .marks(request.getMarks())
                .test(test)
                .build();

        // 7. Save question
        return questionRepository.save(question);
    }
}