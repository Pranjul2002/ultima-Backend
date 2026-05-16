package com.edutech.edutechbackend.test.service;

import com.edutech.edutechbackend.Purchase.entity.Purchase;
import com.edutech.edutechbackend.Purchase.repository.PurchaseRepository;
import com.edutech.edutechbackend.question.dto.StudentAnswerRequest;
import com.edutech.edutechbackend.question.dto.StudentQuestionResponse;
import com.edutech.edutechbackend.question.dto.SubmitTestRequest;
import com.edutech.edutechbackend.question.dto.SubmitTestResponse;
import com.edutech.edutechbackend.question.entity.Question;
import com.edutech.edutechbackend.question.repository.QuestionRepository;
import com.edutech.edutechbackend.subject.entity.Subject;
import com.edutech.edutechbackend.subject.repository.SubjectRepository;
import com.edutech.edutechbackend.test.dto.TestCreateRequest;
import com.edutech.edutechbackend.test.dto.TestResponse;
import com.edutech.edutechbackend.test.entity.Test;
import com.edutech.edutechbackend.test.enums.TestTypeFilter;
import com.edutech.edutechbackend.test.repository.TestRepository;
import com.edutech.edutechbackend.question.dto.ReviewQuestionResponse;
import com.edutech.edutechbackend.question.dto.TestReviewResponse;
import com.edutech.edutechbackend.testattempt.dto.TestAttemptResponse;
import com.edutech.edutechbackend.testattempt.entity.TestAttempt;
import com.edutech.edutechbackend.testattempt.repository.TestAttemptRepository;
import com.edutech.edutechbackend.user.entity.User;
import com.edutech.edutechbackend.user.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final PurchaseRepository purchaseRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final TestAttemptRepository testAttemptRepository;

    public List<TestResponse> getTestsByType(User user, TestTypeFilter type) {
        List<Test> tests;

        switch (type) {
            case FREE:
                tests = testRepository.findByIsPaidFalse();
                break;

            case OWNED:
                tests = purchaseRepository.findByStudent(user).stream()
                        .map(Purchase::getTest)
                        .filter(Test::isPaid)
                        .collect(Collectors.toList());
                break;

            case ALL:
                List<Test> freeTests = testRepository.findByIsPaidFalse();
                List<Test> ownedTests = purchaseRepository.findByStudent(user).stream()
                        .map(Purchase::getTest)
                        .filter(Test::isPaid)
                        .collect(Collectors.toList());
                freeTests.addAll(ownedTests);
                tests = freeTests;
                break;

            case PAID_ALL:
                tests = testRepository.findByIsPaidTrue();
                break;

            default:
                throw new RuntimeException("Invalid type filter");
        }

        return tests.stream()
                .map(this::mapToTestResponse)
                .toList();
    }

    public TestResponse createTest(User mentor, TestCreateRequest request) {

        if (mentor.getRole() != Role.MENTOR) {
            throw new RuntimeException("Only mentors can create tests");
        }

        Subject subject = subjectRepository.findByName(request.getSubjectName())
                .orElseGet(() -> {
                    Subject newSubject = Subject.builder()
                            .name(request.getSubjectName())
                            .build();
                    return subjectRepository.save(newSubject);
                });

        double price = 0.0;

        if (request.getIsPaid()) {
            if (request.getPrice() == null || request.getPrice() <= 0) {
                throw new RuntimeException("Paid test must have valid price");
            }
            price = request.getPrice();
        }

        Test test = Test.builder()
                .title(request.getTitle())
                .isPaid(request.getIsPaid())
                .price(price)
                .subject(subject)
                .mentor(mentor)
                .build();

        Test savedTest = testRepository.save(test);
        return mapToTestResponse(savedTest);
    }

    public List<StudentQuestionResponse> getQuestionsForSelectedTest(User user, Long testId) {

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        boolean allowed = false;

        if (!test.isPaid()) {
            allowed = true;
        }

        if (test.isPaid()) {
            allowed = purchaseRepository.findByStudent(user).stream()
                    .anyMatch(purchase -> purchase.getTest().getId().equals(testId));
        }

        if (user.getRole() == Role.MENTOR && test.getMentor().getId().equals(user.getId())) {
            allowed = true;
        }

        if (!allowed) {
            throw new RuntimeException("You are not allowed to access this test");
        }

        List<Question> questions = questionRepository.findByTestId(testId);

        return questions.stream()
                .map(question -> StudentQuestionResponse.builder()
                        .id(question.getId())
                        .questionText(question.getQuestionText())
                        .optionA(question.getOptionA())
                        .optionB(question.getOptionB())
                        .optionC(question.getOptionC())
                        .optionD(question.getOptionD())
                        .marks(question.getMarks())
                        .build())
                .toList();
    }

    @Transactional
    public SubmitTestResponse submitTest(User user, Long testId, SubmitTestRequest request) {

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        boolean allowed = false;

        if (!test.isPaid()) {
            allowed = true;
        }

        if (test.isPaid()) {
            allowed = purchaseRepository.findByStudent(user).stream()
                    .anyMatch(purchase -> purchase.getTest().getId().equals(testId));
        }

        if (user.getRole() == Role.MENTOR && test.getMentor().getId().equals(user.getId())) {
            allowed = true;
        }

        if (!allowed) {
            throw new RuntimeException("You are not allowed to attempt this test");
        }

        List<Question> allQuestions = questionRepository.findByTestId(testId);

        int totalQuestions = allQuestions.size();
        int attemptedQuestions = request.getAnswers().size();
        int correctAnswers = 0;
        int wrongAnswers = 0;
        int score = 0;
        int totalMarks = allQuestions.stream()
                .mapToInt(Question::getMarks)
                .sum();

        for (StudentAnswerRequest answerRequest : request.getAnswers()) {

            Question question = questionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found: " + answerRequest.getQuestionId()));

            if (!question.getTest().getId().equals(testId)) {
                throw new RuntimeException("Question does not belong to this test");
            }

            String selectedAnswer = answerRequest.getSelectedAnswer().toUpperCase();
            String correctAnswer = question.getCorrectAnswer().toUpperCase();

            if (selectedAnswer.equals(correctAnswer)) {
                correctAnswers++;
                score += question.getMarks();
            } else {
                wrongAnswers++;
            }
        }

        testAttemptRepository.deleteByStudentAndTest(user, test);

        TestAttempt newAttempt = TestAttempt.builder()
                .student(user)
                .test(test)
                .totalQuestions(totalQuestions)
                .attemptedQuestions(attemptedQuestions)
                .correctAnswers(correctAnswers)
                .wrongAnswers(wrongAnswers)
                .score(score)
                .totalMarks(totalMarks)
                .submittedAt(LocalDateTime.now())
                .build();

        testAttemptRepository.save(newAttempt);

        return SubmitTestResponse.builder()
                .testId(testId)
                .totalQuestions(totalQuestions)
                .attemptedQuestions(attemptedQuestions)
                .correctAnswers(correctAnswers)
                .wrongAnswers(wrongAnswers)
                .score(score)
                .totalMarks(totalMarks)
                .build();
    }

    public List<TestAttemptResponse> getMyAttempts(User user) {
        return testAttemptRepository.findByStudentOrderBySubmittedAtDesc(user).stream()
                .map(attempt -> TestAttemptResponse.builder()
                        .attemptId(attempt.getId())
                        .testId(attempt.getTest().getId())
                        .testTitle(attempt.getTest().getTitle())
                        .totalQuestions(attempt.getTotalQuestions())
                        .attemptedQuestions(attempt.getAttemptedQuestions())
                        .correctAnswers(attempt.getCorrectAnswers())
                        .wrongAnswers(attempt.getWrongAnswers())
                        .score(attempt.getScore())
                        .totalMarks(attempt.getTotalMarks())
                        .submittedAt(attempt.getSubmittedAt())
                        .build())
                .toList();
    }

    public TestReviewResponse getTestReview(User user, Long testId) {

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        TestAttempt attempt = testAttemptRepository.findByStudentAndTest(user, test)
                .orElseThrow(() -> new RuntimeException("No completed attempt found for this test"));

        List<Question> questions = questionRepository.findByTestId(testId);

        TestAttemptResponse attemptDto = TestAttemptResponse.builder()
                .attemptId(attempt.getId())
                .testId(testId)
                .testTitle(test.getTitle())
                .totalQuestions(attempt.getTotalQuestions())
                .attemptedQuestions(attempt.getAttemptedQuestions())
                .correctAnswers(attempt.getCorrectAnswers())
                .wrongAnswers(attempt.getWrongAnswers())
                .score(attempt.getScore())
                .totalMarks(attempt.getTotalMarks())
                .submittedAt(attempt.getSubmittedAt())
                .build();

        List<ReviewQuestionResponse> questionDtos = questions.stream()
                .map(q -> ReviewQuestionResponse.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .marks(q.getMarks())
                        .correctAnswer(q.getCorrectAnswer())   // ← exposed only here
                        .build())
                .toList();

        return TestReviewResponse.builder()
                .attempt(attemptDto)
                .questions(questionDtos)
                .build();
    }

    private TestResponse mapToTestResponse(Test test) {
        return TestResponse.builder()
                .id(test.getId())
                .title(test.getTitle())
                .isPaid(test.isPaid())
                .price(test.getPrice())
                .subjectId(test.getSubject() != null ? test.getSubject().getId() : null)
                .subjectName(test.getSubject() != null ? test.getSubject().getName() : null)
                .mentorId(test.getMentor() != null ? test.getMentor().getId() : null)
                .mentorName(test.getMentor() != null ? test.getMentor().getUsername() : null)
                .build();
    }
}