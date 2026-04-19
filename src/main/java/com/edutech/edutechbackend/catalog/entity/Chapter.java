package com.edutech.edutechbackend.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * A single chapter inside a book.
 * chapterId mirrors BOOK_CHAPTERS[bookSlug].chapters[].id, e.g. "light-reflection".
 * linkedTestId points to the backend Test entity — this is the bridge to /api/tests.
 */
@Entity
@Table(
        name = "catalog_chapter",
        uniqueConstraints = @UniqueConstraint(columnNames = {"book_slug", "chapter_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. "light-reflection" — matches frontend chapter id */
    @Column(name = "chapter_id", nullable = false)
    private String chapterId;

    /** Parent book slug e.g. "ncert-physics-class-10" */
    @Column(name = "book_slug", nullable = false)
    private String bookSlug;

    private Integer number;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private String duration;       // "~45 min"
    private Integer questionCount;

    @Column(name = "coming_soon")
    @Builder.Default
    private Boolean comingSoon = false;

    /**
     * The backend Test.id linked to this chapter.
     * Set by CatalogSeeder (or admin) after the mentor creates a test.
     * Null means "no live test yet" — frontend shows "Soon" badge.
     */
    @Column(name = "linked_test_id")
    private Long linkedTestId;
}