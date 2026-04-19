package com.edutech.edutechbackend.catalog.repository;

import com.edutech.edutechbackend.catalog.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findByBookSlugOrderByNumber(String bookSlug);

    Optional<Chapter> findByBookSlugAndChapterId(String bookSlug, String chapterId);
}