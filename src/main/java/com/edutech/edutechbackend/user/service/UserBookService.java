package com.edutech.edutechbackend.user.service;

import com.edutech.edutechbackend.Purchase.entity.Purchase;
import com.edutech.edutechbackend.Purchase.repository.PurchaseRepository;
import com.edutech.edutechbackend.catalog.repository.ChapterRepository;
import com.edutech.edutechbackend.user.dto.BookDto;
import com.edutech.edutechbackend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Assembles the "My Books" library for the logged-in user:
 *
 *   1. FREE books  — NCERT Physics, Chemistry, Maths (classes 10, 11, 12).
 *                    These are visible to every authenticated user automatically.
 *   2. PURCHASED books — any book slug found in the user's Purchase records
 *                        that is NOT already in the free list.
 *
 * Book metadata (title, subtitle, chapter count, etc.) is derived from the
 * catalog chapter table so it stays in sync with the seeded data.
 */
@Service
@RequiredArgsConstructor
public class UserBookService {

    private final ChapterRepository   chapterRepository;
    private final PurchaseRepository  purchaseRepository;
    private final UserProfileService  userProfileService;

    // ── Free NCERT book slugs ─────────────────────────────────────────────────
    // Physics, Chemistry and Maths for classes 10, 11, 12 are always free.
    private static final Set<String> FREE_BOOK_SLUGS = Set.of(
            "ncert-physics-class-10",
            "ncert-physics-class-11",
            "ncert-physics-class-12",
            "ncert-chemistry-class-10",
            "ncert-chemistry-class-11",
            "ncert-chemistry-class-12",
            "ncert-maths-class-10",
            "ncert-maths-class-11",
            "ncert-maths-class-12"
    );

    // ── Static metadata map  (slug → partial BookDto) ─────────────────────────
    // Title / subtitle / subject / classLabel are not stored in the Chapter
    // entity, so we keep a lightweight lookup here.  Add new books here as the
    // catalog grows.
    private static final Map<String, BookMeta> BOOK_META_MAP;

    static {
        Map<String, BookMeta> m = new LinkedHashMap<>();

        // Class 10
        m.put("ncert-physics-class-10",   new BookMeta("NCERT Physics Foundations",    "Physics · Class 10",    "Physics",     "Class 10"));
        m.put("ncert-chemistry-class-10", new BookMeta("NCERT Chemistry Basics",        "Chemistry · Class 10",  "Chemistry",   "Class 10"));
        m.put("ncert-maths-class-10",     new BookMeta("NCERT Mathematics Class 10",    "Maths · Class 10",      "Mathematics", "Class 10"));
        m.put("ncert-biology-class-10",   new BookMeta("NCERT Biology Class 10",        "Biology · Class 10",    "Biology",     "Class 10"));

        // Class 11
        m.put("ncert-physics-class-11",   new BookMeta("NCERT Physics Class 11",        "Physics · Class 11",    "Physics",     "Class 11"));
        m.put("ncert-chemistry-class-11", new BookMeta("NCERT Chemistry Class 11",      "Chemistry · Class 11",  "Chemistry",   "Class 11"));
        m.put("ncert-maths-class-11",     new BookMeta("NCERT Mathematics Class 11",    "Maths · Class 11",      "Mathematics", "Class 11"));
        m.put("ncert-biology-class-11",   new BookMeta("NCERT Biology Class 11",        "Biology · Class 11",    "Biology",     "Class 11"));

        // Class 12
        m.put("ncert-physics-class-12",   new BookMeta("NCERT Physics Class 12",        "Physics · Class 12",    "Physics",     "Class 12"));
        m.put("ncert-chemistry-class-12", new BookMeta("NCERT Chemistry Class 12",      "Chemistry · Class 12",  "Chemistry",   "Class 12"));
        m.put("ncert-maths-class-12",     new BookMeta("NCERT Mathematics Class 12",    "Maths · Class 12",      "Mathematics", "Class 12"));
        m.put("ncert-biology-class-12",   new BookMeta("NCERT Biology Class 12",        "Biology · Class 12",    "Biology",     "Class 12"));

        BOOK_META_MAP = Collections.unmodifiableMap(m);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the full book library for the currently authenticated user.
     * Free books come first (ordered by class then subject), purchased books follow.
     */
    public List<BookDto> getMyBooks() {
        User user = userProfileService.getCurrentUser();

        // 1. Chapter counts per book slug from the DB
        Map<String, Long> chapterCounts = buildChapterCountMap();

        // 2. Purchased book slugs (excluding any that are already free)
        Set<String> purchasedSlugs = getPurchasedBookSlugs(user);

        List<BookDto> result = new ArrayList<>();

        // 3. Free books first
        for (String slug : FREE_BOOK_SLUGS) {
            result.add(buildBookDto(slug, true, chapterCounts));
        }

        // 4. Purchased books (not already in free list)
        for (String slug : purchasedSlugs) {
            if (!FREE_BOOK_SLUGS.contains(slug)) {
                result.add(buildBookDto(slug, false, chapterCounts));
            }
        }

        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds a map of bookSlug → chapter count using the catalog_chapter table.
     */
    private Map<String, Long> buildChapterCountMap() {
        return chapterRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        ch -> ch.getBookSlug(),
                        Collectors.counting()
                ));
    }

    /**
     * Returns the distinct set of book slugs purchased by the given user.
     * The Purchase entity currently references a Test, not a book directly.
     * We derive the book slug from the chapter that owns that test
     * (chapter.linkedTestId == purchase.test.id).
     */
    private Set<String> getPurchasedBookSlugs(User user) {
        List<Purchase> purchases = purchaseRepository.findByStudent(user);
        if (purchases.isEmpty()) return Collections.emptySet();

        Set<Long> testIds = purchases.stream()
                .filter(p -> p.getTest() != null)
                .map(p -> p.getTest().getId())
                .collect(Collectors.toSet());

        return chapterRepository.findAll()
                .stream()
                .filter(ch -> ch.getLinkedTestId() != null && testIds.contains(ch.getLinkedTestId()))
                .map(ch -> ch.getBookSlug())
                .collect(Collectors.toSet());
    }

    private BookDto buildBookDto(String slug, boolean isFree, Map<String, Long> chapterCounts) {
        BookMeta meta = BOOK_META_MAP.getOrDefault(slug,
                new BookMeta(slug, slug, "General", "Unknown"));

        long chapters = chapterCounts.getOrDefault(slug, 0L);

        return BookDto.builder()
                .slug(slug)
                .classSlug(slug)          // frontend uses this for URL: /products/books/{classSlug}/{bookSlug}
                .title(meta.title())
                .subtitle(meta.subtitle())
                .description("Chapter-wise learning, practice, and tests for " + meta.title() + ".")
                .subject(meta.subject())
                .classLabel(meta.classLabel())
                .chapterCount((int) chapters)
                .questionCount(0)         // extend later if needed
                .isFree(isFree)
                .build();
    }

    // ── Internal record ───────────────────────────────────────────────────────

    private record BookMeta(String title, String subtitle, String subject, String classLabel) {}
}
