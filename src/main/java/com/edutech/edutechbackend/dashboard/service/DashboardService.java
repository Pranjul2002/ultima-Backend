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

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DashboardService
 * ────────────────────────────────────────────────────────────────────────────
 * Returns the full dashboard payload for the logged-in user.
 *
 * TESTS section (tests[]):
 *   • All free tests are always included (isPaid = false).
 *   • For each test the user has already attempted, we attach the latest
 *     attempt's score / status.  Unattempted tests get status = "pending".
 * ────────────────────────────────────────────────────────────────────────────
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserProfileService   userProfileService;
    private final TestRepository       testRepository;
    private final TestAttemptRepository testAttemptRepository;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d MMM yyyy");

    // ── public API ───────────────────────────────────────────────────────────
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

    // ── private helpers ──────────────────────────────────────────────────────

    /**
     * Builds the list of TestItemDto shown in the "My Tests" dashboard tab.
     *
     * Logic:
     *  1. Fetch all free tests from DB.
     *  2. Fetch all attempts by this user and index them by testId.
     *  3. For each free test:
     *       - If an attempt exists → status = "passed" or "failed", score filled.
     *       - Else                 → status = "pending", score = null.
     */
    private List<TestItemDto> buildTestItems(User user) {

        List<Test> freeTests = testRepository.findByIsPaidFalse();

        // Index: testId → latest attempt (already ordered by submittedAt desc by the repo)
        List<TestAttempt> attempts = testAttemptRepository
                .findByStudentOrderBySubmittedAtDesc(user);

        Map<Long, TestAttempt> latestByTestId = attempts.stream()
                .collect(Collectors.toMap(
                        a -> a.getTest().getId(),
                        a -> a,
                        (first, second) -> first   // keep most recent
                ));

        return freeTests.stream()
                .map(test -> toTestItemDto(test, latestByTestId.get(test.getId())))
                .collect(Collectors.toList());
    }

    private TestItemDto toTestItemDto(Test test, TestAttempt attempt) {

        String subjectName = test.getSubject() != null
                ? test.getSubject().getName() : "";

        // Total marks from questions (sum of marks per question)
        int totalMarks = test.getQuestions().stream()
                .mapToInt(q -> q.getMarks() != null ? q.getMarks() : 0)
                .sum();

        if (attempt == null) {
            // Not yet attempted
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

        // Attempted — determine pass/fail (pass threshold: >= 40 %)
        int score      = attempt.getScore() != null ? attempt.getScore() : 0;
        int totalM     = attempt.getTotalMarks() != null ? attempt.getTotalMarks() : totalMarks;
        double pct     = totalM > 0 ? (double) score / totalM * 100 : 0;
        String status  = pct >= 40.0 ? "passed" : "failed";

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