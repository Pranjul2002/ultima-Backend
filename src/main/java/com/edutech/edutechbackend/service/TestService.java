package com.edutech.edutechbackend.service;

import com.edutech.edutechbackend.dto.*;
import com.edutech.edutechbackend.entity.*;
import com.edutech.edutechbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final TestSessionRepository testSessionRepository;
    private final TestAnswerRepository testAnswerRepository;
    private final StudentRepository studentRepository;

    // ════════════════════════════════════════════════
    // CREATE TEST PAPER (Admin only)
    // ════════════════════════════════════════════════
    public TestResponseDTO createTest(CreateTestRequestDTO request, String adminEmail) {

        // ── STEP 1: Find the admin from DB ────────────
        Student admin = studentRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        // ↑ we get adminEmail from JWT token
        //   find the full Student object from DB
        //   we need admin.getId() for created_by_id column

        // ── STEP 2: Find the subject ──────────────────
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException(
                        "Subject not found with id: " + request.getSubjectId()));
        // ↑ admin sends subjectId in request
        //   we find the actual Subject object
        //   if subject doesn't exist → 404 error

        // ── STEP 3: Check duplicate title ─────────────
        if (testRepository.existsByTitleAndSubjectId(
                request.getTitle(), request.getSubjectId())) {
            throw new RuntimeException(
                    "Test with this title already exists in this subject");
        }
        // ↑ prevents: two tests named "Math Mid-Term 2026"
        //   under the same subject

        // ── STEP 4: Build and save test ───────────────
        Test test = Test.builder()
                .title(request.getTitle())
                .subject(subject)
                // ↑ links to Subject object
                //   JPA stores subject.getId() as subject_id in DB
                .createdBy(admin)
                // ↑ links to admin Student object
                //   JPA stores admin.getId() as created_by_id in DB
                .build();

        Test saved = testRepository.save(test);
        // ↑ INSERT INTO tests (title, subject_id, created_by_id, created_at)
        //   VALUES (?, ?, ?, ?)

        // ── STEP 5: Build response ────────────────────
        return TestResponseDTO.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .subjectName(subject.getName())
                .createdByName(admin.getName())
                .totalQuestions(0)
                // ↑ newly created test has 0 questions
                .createdAt(saved.getCreatedAt().toString())
                .build();
    }

    // ════════════════════════════════════════════════
    // GET MY TESTS (Admin sees only their own tests)
    // ════════════════════════════════════════════════
    public List<TestResponseDTO> getMyTests(String adminEmail) {

        Student admin = studentRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        return testRepository.findByCreatedBy(admin)
                // ↑ SELECT * FROM tests WHERE created_by_id = ?
                //   returns only THIS admin's tests
                //   Admin Raj never sees Admin Priya's tests

                .stream()
                .map(test -> TestResponseDTO.builder()
                        .id(test.getId())
                        .title(test.getTitle())
                        .subjectName(test.getSubject().getName())
                        .createdByName(admin.getName())
                        .totalQuestions(questionRepository.countByTestId(test.getId()))
                        // ↑ SELECT COUNT(*) FROM questions WHERE test_id = ?
                        //   shows how many questions this test has so far
                        .createdAt(test.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════
    // ADD QUESTION TO TEST (Admin only)
    // ════════════════════════════════════════════════
    public QuestionResponseDTO addQuestion(
            Long testId,
            CreateQuestionRequestDTO request,
            String adminEmail) {

        // ── STEP 1: Find the test ─────────────────────
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException(
                        "Test not found with id: " + testId));

        // ── STEP 2: Verify admin owns this test ───────
        Student admin = studentRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!test.getCreatedBy().getId().equals(admin.getId())) {
            throw new RuntimeException(
                    "You can only add questions to your own tests");
        }
        // ↑ security check!
        //   Admin Raj can't add questions to Admin Priya's test
        //   even if Raj is logged in as admin
        //   only the creator of the test can modify it

        // ── STEP 3: Build and save question ──────────
        Question question = Question.builder()
                .test(test)
                .questionText(request.getQuestionText())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .correctOption(request.getCorrectOption())
                // ↑ stored in DB
                //   NEVER sent to student in response
                .build();

        Question saved = questionRepository.save(question);

        // ── STEP 4: Build response (NO correctOption!) ─
        return QuestionResponseDTO.builder()
                .id(saved.getId())
                .questionText(saved.getQuestionText())
                .optionA(saved.getOptionA())
                .optionB(saved.getOptionB())
                .optionC(saved.getOptionC())
                .optionD(saved.getOptionD())
                // ↑ notice: correctOption is NOT included
                //   admin sees it in DB but we don't return it
                //   consistent with what student sees
                .build();
    }

    // ════════════════════════════════════════════════
// ADD MULTIPLE QUESTIONS AT ONCE (Admin only)
// ════════════════════════════════════════════════
    public List<QuestionResponseDTO> addQuestions(
            Long testId,
            List<CreateQuestionRequestDTO> requests,
            String adminEmail) {

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException(
                        "Test not found with id: " + testId));

        Student admin = studentRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!test.getCreatedBy().getId().equals(admin.getId())) {
            throw new RuntimeException(
                    "You can only add questions to your own tests");
        }

        // build all questions at once
        List<Question> questions = requests.stream()
                .map(request -> Question.builder()
                        .test(test)
                        .questionText(request.getQuestionText())
                        .optionA(request.getOptionA())
                        .optionB(request.getOptionB())
                        .optionC(request.getOptionC())
                        .optionD(request.getOptionD())
                        .correctOption(request.getCorrectOption())
                        .build())
                .collect(Collectors.toList());

        // save all in one DB call
        List<Question> saved = questionRepository.saveAll(questions);
        // ↑ INSERT all questions in one batch
        //   much more efficient than saving one by one

        return saved.stream()
                .map(q -> QuestionResponseDTO.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .build())
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════
    // GET TESTS BY SUBJECT (Student browsing)
    // ════════════════════════════════════════════════
    public List<TestResponseDTO> getTestsBySubject(Long subjectId) {

        subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException(
                        "Subject not found with id: " + subjectId));
        // ↑ validate subject exists first
        //   if student sends wrong subjectId → clear error

        return testRepository.findBySubjectId(subjectId)
                // ↑ SELECT * FROM tests WHERE subject_id = ?
                //   returns ALL tests under this subject
                //   from ALL admins

                .stream()
                .map(test -> TestResponseDTO.builder()
                        .id(test.getId())
                        .title(test.getTitle())
                        .subjectName(test.getSubject().getName())
                        .createdByName(test.getCreatedBy().getName())
                        // ↑ student sees which admin/teacher made this
                        .totalQuestions(questionRepository.countByTestId(test.getId()))
                        .createdAt(test.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════
    // GET QUESTIONS FOR A TEST (Student taking test)
    // ════════════════════════════════════════════════
    public List<QuestionResponseDTO> getQuestionsForTest(Long testId) {

        testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException(
                        "Test not found with id: " + testId));
        // ↑ validate test exists

        return questionRepository.findByTestId(testId)
                // ↑ SELECT * FROM questions WHERE test_id = ?

                .stream()
                .map(question -> QuestionResponseDTO.builder()
                        .id(question.getId())
                        .questionText(question.getQuestionText())
                        .optionA(question.getOptionA())
                        .optionB(question.getOptionB())
                        .optionC(question.getOptionC())
                        .optionD(question.getOptionD())
                        // ↑ correctOption intentionally excluded!
                        //   student must not see answers before submitting
                        .build())
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════
    // SUBMIT TEST (Student submitting answers)
    // ════════════════════════════════════════════════
    @Transactional
    // ↑ everything in this method is ONE database transaction
    //   if anything fails midway:
    //   → session not saved, answers not saved, nothing saved
    //   → DB stays clean, no partial data
    public TestResultResponseDTO submitTest(
            SubmitTestRequestDTO request,
            String studentEmail) {

        // ── STEP 1: Find student ──────────────────────
        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // ── STEP 2: Find test ─────────────────────────
        Test test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new RuntimeException("Test not found"));

        // ── STEP 3: Score the answers ─────────────────
        int score = 0;
        // ↑ starts at 0, increments for each correct answer

        List<TestResultResponseDTO.AnswerResultDTO> answerResults =
                new java.util.ArrayList<>();
        // ↑ will hold detailed result for each question
        //   shown to student after submission

        List<TestAnswer> answersToSave = new java.util.ArrayList<>();
        // ↑ will hold TestAnswer entities to save to DB

        for (SubmitTestRequestDTO.AnswerDTO answerDTO : request.getAnswers()) {
            // ↑ loop through each answer student submitted

            // find the question from DB
            Question question = questionRepository
                    .findById(answerDTO.getQuestionId())
                    .orElseThrow(() -> new RuntimeException(
                            "Question not found: " + answerDTO.getQuestionId()));

            // check if answer is correct
            boolean isCorrect = question.getCorrectOption()
                    .equals(answerDTO.getSelectedOption());
            // ↑ compare DB's correctOption with student's selectedOption
            //   AnswerOption.B == AnswerOption.B → true
            //   AnswerOption.A == AnswerOption.B → false

            if (isCorrect) score++;
            // ↑ increment score for each correct answer

            // build TestAnswer entity to save
            answersToSave.add(TestAnswer.builder()
                    .question(question)
                    .selectedOption(answerDTO.getSelectedOption())
                    .correct(isCorrect)
                    // session set after session is saved below
                    .build());

            // build result DTO for this question
            answerResults.add(TestResultResponseDTO.AnswerResultDTO.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .selectedOption(answerDTO.getSelectedOption().name())
                    .correctOption(question.getCorrectOption().name())
                    // ↑ NOW we reveal correct answer
                    //   test already submitted — it's fine to show
                    .correct(isCorrect)
                    .build());
        }

        // ── STEP 4: Calculate percentage ──────────────
        int totalQuestions = request.getAnswers().size();
        double percentage = ((double) score / totalQuestions) * 100;
        // ↑ e.g. 7/10 * 100 = 70.0

        // ── STEP 5: Save TestSession ───────────────────
        TestSession session = TestSession.builder()
                .student(student)
                .test(test)
                .score(score)
                .totalQuestions(totalQuestions)
                .percentage(percentage)
                .build();

        TestSession savedSession = testSessionRepository.save(session);
        // ↑ INSERT INTO test_sessions
        //   (student_id, test_id, score, total_questions, percentage, taken_at)
        //   VALUES (?, ?, ?, ?, ?, ?)

        // ── STEP 6: Save all TestAnswers ──────────────
        for (TestAnswer answer : answersToSave) {
            answer.setSession(savedSession);
            // ↑ now we have the session id
            //   link each answer to this session
        }
        testAnswerRepository.saveAll(answersToSave);
        // ↑ INSERT INTO test_answers for each answer
        //   all saved in one batch — efficient

        // ── STEP 7: Build and return result ───────────
        return TestResultResponseDTO.builder()
                .sessionId(savedSession.getId())
                .testTitle(test.getTitle())
                .score(score)
                .totalQuestions(totalQuestions)
                .percentage(percentage)
                .takenAt(savedSession.getTakenAt().toString())
                .answerResults(answerResults)
                .build();
    }

    // ════════════════════════════════════════════════
    // GET TEST HISTORY (Student's past attempts)
    // ════════════════════════════════════════════════
    public List<TestHistoryResponseDTO> getTestHistory(String studentEmail) {

        Student student = studentRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return testSessionRepository
                .findByStudentIdOrderByTakenAtDesc(student.getId())
                // ↑ SELECT * FROM test_sessions
                //   WHERE student_id = ?
                //   ORDER BY taken_at DESC
                //   newest attempts first

                .stream()
                .map(session -> TestHistoryResponseDTO.builder()
                        .sessionId(session.getId())
                        .testTitle(session.getTest().getTitle())
                        .subjectName(session.getTest().getSubject().getName())
                        .score(session.getScore())
                        .totalQuestions(session.getTotalQuestions())
                        .percentage(session.getPercentage())
                        .takenAt(session.getTakenAt().toString())
                        .build())
                .collect(Collectors.toList());
    }
}