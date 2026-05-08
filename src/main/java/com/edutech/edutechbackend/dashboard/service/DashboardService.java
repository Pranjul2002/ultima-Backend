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

    private final UserProfileService     userProfileService;
    private final TestRepository         testRepository;
    private final TestAttemptRepository  testAttemptRepository;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d MMM yyyy");

    /**
     * @Transactional(readOnly=true) keeps ONE Hibernate session open for
     * the entire method — required so that any lazy associations accessed
     * inside mappers don't hit a "session closed" error.
     *
     * UserProfileService.getCurrentUser() re-fetches the User from the DB
     * (not the detached SecurityContext principal), so all lazy fields on
     * the User are accessible within this same session.
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

        // JOIN FETCH loads tests + questions + subject in a single SQL query.
        // No N+1, no lazy-load after session close.
        List<Test> freeTests = testRepository.findFreeTestsWithQuestions();

        List<TestAttempt> attempts =
                testAttemptRepository.findByStudentOrderBySubmittedAtDesc(user);

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

        // Safe — questions already loaded via JOIN FETCH
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
        String date   = attempt.getSubmittedAt() != null
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