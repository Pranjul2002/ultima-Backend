package com.edutech.edutechbackend.dashboard.service;

import com.edutech.edutechbackend.dashboard.dto.DashboardResponse;
import com.edutech.edutechbackend.dashboard.dto.DashboardResponse.TestItemDto;
import com.edutech.edutechbackend.test.entity.Test;
import com.edutech.edutechbackend.test.repository.TestRepository;
import com.edutech.edutechbackend.testattempt.entity.TestAttempt;
import com.edutech.edutechbackend.testattempt.repository.TestAttemptRepository;
import com.edutech.edutechbackend.user.entity.User;
import com.edutech.edutechbackend.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserProfileService    userProfileService;
    private final TestRepository        testRepository;
    private final TestAttemptRepository testAttemptRepository;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d MMM yyyy");

    /**
     * @Transactional is REQUIRED here.
     *
     * Why: Test.questions is a lazy @OneToMany collection. Even though we use
     * JOIN FETCH in the repository query, the @Transactional annotation ensures
     * Hibernate keeps the session open for the full duration of this method.
     * Without it, accessing any lazy association after the repo call closes the
     * session and throws LazyInitializationException → HTTP 500.
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {

        User user = userProfileService.getCurrentUser();

        List<TestItemDto> testItems = buildTestItems(user);

        return DashboardResponse.builder()
                .user(DashboardResponse.CurrentUserDto.builder()
                        .name(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .overview(DashboardResponse.OverviewDto.builder()
                        .stats(Collections.emptyList())
                        .progress(Collections.emptyList())
                        .activity(Collections.emptyList())
                        .build())
                .profile(userProfileService.getProfile())
                .tests(testItems)
                .settings(userProfileService.getSettings())
                .build();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private List<TestItemDto> buildTestItems(User user) {

        // Uses JOIN FETCH — loads tests + questions in ONE query, no N+1, no lazy errors
        List<Test> freeTests = testRepository.findFreeTestsWithQuestions();

        // Fetch all attempts by this user, indexed by testId for O(1) lookup
        List<TestAttempt> attempts =
                testAttemptRepository.findByStudentOrderBySubmittedAtDesc(user);

        Map<Long, TestAttempt> latestByTestId = attempts.stream()
                .collect(Collectors.toMap(
                        a -> a.getTest().getId(),
                        a -> a,
                        (first, second) -> first  // keep most recent
                ));

        return freeTests.stream()
                .map(test -> toTestItemDto(test, latestByTestId.get(test.getId())))
                .collect(Collectors.toList());
    }

    private TestItemDto toTestItemDto(Test test, TestAttempt attempt) {

        String subjectName = test.getSubject() != null
                ? test.getSubject().getName() : "";

        // Sum marks from questions — safe because JOIN FETCH already loaded them
        int totalMarks = test.getQuestions().stream()
                .mapToInt(q -> q.getMarks() != null ? q.getMarks() : 0)
                .sum();

        if (attempt == null) {
            return TestItemDto.builder()
                    .id(test.getId())
                    .title(test.getTitle())
                    .subject(subjectName)
                    .date(null)
                    .score(null)
                    .total(totalMarks > 0 ? totalMarks : test.getQuestions().size() * 4)
                    .duration(null)
                    .status("pending")
                    .build();
        }

        int score     = attempt.getScore() != null ? attempt.getScore() : 0;
        int totalM    = attempt.getTotalMarks() != null ? attempt.getTotalMarks() : totalMarks;
        double pct    = totalM > 0 ? (double) score / totalM * 100 : 0;
        String status = pct >= 40.0 ? "passed" : "failed";

        String date = attempt.getSubmittedAt() != null
                ? attempt.getSubmittedAt().format(DATE_FMT) : null;

        return TestItemDto.builder()
                .id(test.getId())
                .title(test.getTitle())
                .subject(subjectName)
                .date(date)
                .score(score)
                .total(totalM > 0 ? totalM : test.getQuestions().size() * 4)
                .duration(null)
                .status(status)
                .build();
    }
}