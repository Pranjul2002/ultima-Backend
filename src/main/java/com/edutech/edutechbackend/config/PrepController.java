package com.edutech.edutechbackend.config;

import com.edutech.edutechbackend.question.repository.QuestionRepository;
import com.edutech.edutechbackend.test.entity.Test;
import com.edutech.edutechbackend.test.repository.TestRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PrepController
 * ──────────────────────────────────────────────────────────────────────────────
 * Public endpoints (no JWT) for competitive-exam product pages.
 *
 *  GET /api/prep/{exam}  →  list of { id, title, isPaid, questionCount }
 *
 *  exam values:  jee | neet | gate
 * ──────────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/prep")
@RequiredArgsConstructor
public class PrepController {

    private final TestRepository     testRepository;
    private final QuestionRepository questionRepository;   // ← used for COUNT query

    private static final Map<String, List<String>> EXAM_TITLES = Map.of(
            "jee",  List.of(PrepTestSeeder.JEE_PHYSICS, PrepTestSeeder.JEE_CHEMISTRY, PrepTestSeeder.JEE_MATHS),
            "neet", List.of(PrepTestSeeder.NEET_PHYSICS, PrepTestSeeder.NEET_BIO, PrepTestSeeder.NEET_CHEMISTRY),
            "gate", List.of(PrepTestSeeder.GATE_TEST)
    );

    @GetMapping("/{exam}")
    public List<PrepTestInfo> getExamTests(@PathVariable String exam) {
        List<String> titles = EXAM_TITLES.getOrDefault(exam.toLowerCase(), List.of());
        if (titles.isEmpty()) return List.of();

        List<Test> all = testRepository.findAll();

        return titles.stream()
                .flatMap(title -> all.stream()
                        .filter(t -> t.getTitle().equals(title))
                        .findFirst()
                        .map(t -> PrepTestInfo.builder()
                                .id(t.getId())
                                .title(t.getTitle())
                                .isPaid(t.isPaid())
                                // Use a COUNT query — avoids LazyInitializationException
                                .questionCount((int) questionRepository.countByTestId(t.getId()))
                                .build())
                        .stream()
                )
                .toList();
    }

    @Data
    @Builder
    public static class PrepTestInfo {
        private Long    id;
        private String  title;
        private boolean isPaid;
        private int     questionCount;
    }
}