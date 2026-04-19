package com.edutech.edutechbackend.user.dto;

import lombok.*;

/**
 * Represents a single book entry returned by GET /api/user/my-books.
 * Includes both free NCERT books and books the user has purchased.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {

    /** e.g. "ncert-physics-class-10" */
    private String slug;

    /** e.g. "ncert-physics-class-10" — same as slug, kept for URL-building on the frontend */
    private String classSlug;

    private String title;
    private String subtitle;
    private String description;

    /** e.g. "Physics", "Chemistry", "Mathematics" */
    private String subject;

    /** e.g. "Class 10", "Class 11", "Class 12" */
    private String classLabel;

    private Integer chapterCount;
    private Integer questionCount;

    /** true  → NCERT free book (always visible to all logged-in users)
     *  false → purchased by this user */
    private Boolean isFree;
}
