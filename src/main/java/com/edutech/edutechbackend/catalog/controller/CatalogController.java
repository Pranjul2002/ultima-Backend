package com.edutech.edutechbackend.catalog.controller;

import com.edutech.edutechbackend.catalog.dto.ChapterResponse;
import com.edutech.edutechbackend.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public (no JWT needed) catalog endpoints consumed by the Next.js frontend.
 *
 *  GET /api/catalog/chapters/{bookSlug}
 *      → all chapters for a book  (used by book detail page)
 *
 *  GET /api/catalog/chapters/{bookSlug}/{chapterId}
 *      → single chapter  (used by ChapterActions to resolve linkedTestId)
 */
@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/chapters/{bookSlug}")
    public List<ChapterResponse> getChapters(@PathVariable String bookSlug) {
        return catalogService.getChapters(bookSlug);
    }

    @GetMapping("/chapters/{bookSlug}/{chapterId}")
    public ChapterResponse getChapter(
            @PathVariable String bookSlug,
            @PathVariable String chapterId
    ) {
        return catalogService.getChapter(bookSlug, chapterId);
    }
}