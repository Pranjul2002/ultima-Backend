package com.edutech.edutechbackend.catalog.service;

import com.edutech.edutechbackend.catalog.dto.ChapterResponse;
import com.edutech.edutechbackend.catalog.entity.Chapter;
import com.edutech.edutechbackend.catalog.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ChapterRepository chapterRepository;

    /** All chapters of a book, ordered by chapter number. */
    public List<ChapterResponse> getChapters(String bookSlug) {
        return chapterRepository.findByBookSlugOrderByNumber(bookSlug)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Single chapter detail — used by ChapterActions to resolve the linkedTestId. */
    public ChapterResponse getChapter(String bookSlug, String chapterId) {
        Chapter c = chapterRepository
                .findByBookSlugAndChapterId(bookSlug, chapterId)
                .orElseThrow(() -> new RuntimeException(
                        "Chapter not found: " + bookSlug + "/" + chapterId));
        return toResponse(c);
    }

    // ── mapper ────────────────────────────────────────────────────────────────

    private ChapterResponse toResponse(Chapter c) {
        return ChapterResponse.builder()
                .chapterId(c.getChapterId())
                .bookSlug(c.getBookSlug())
                .number(c.getNumber())
                .title(c.getTitle())
                .description(c.getDescription())
                .duration(c.getDuration())
                .questionCount(c.getQuestionCount())
                .comingSoon(c.getComingSoon())
                .linkedTestId(c.getLinkedTestId())   // ← THE bridge to /api/tests
                .build();
    }
}