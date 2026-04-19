package com.edutech.edutechbackend.upskilling.service;

import com.edutech.edutechbackend.upskilling.dto.SourceStatusResponse;
import com.edutech.edutechbackend.upskilling.dto.SourceUploadResponse;
import com.edutech.edutechbackend.upskilling.entity.UpskillingSource;
import com.edutech.edutechbackend.upskilling.entity.UpskillingSourceStatus;
import com.edutech.edutechbackend.upskilling.repository.UpskillingSourceRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpskillingSourceService {

    private static final int MAX_SUMMARY_CHARS = 12000;

    private final UpskillingSourceRepository repository;
    private final GroqClientService groq;   // ✅ fixed: was GrokClientService

    @Transactional
    public SourceUploadResponse uploadSource(MultipartFile file) {

        UpskillingSource source = repository.save(
                UpskillingSource.builder()
                        .fileName(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .status(UpskillingSourceStatus.PROCESSING)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        try {
            String text = extractText(file);
            String clipped = text.length() > MAX_SUMMARY_CHARS
                    ? text.substring(0, MAX_SUMMARY_CHARS) : text;

            String summary = groq.generate(
                    "You are a document summarizer. Summarize only from the given text. Be concise.",
                    "Summarize this document:\n\n" + clipped
            );

            source.setExtractedText(text);
            source.setSummary(summary);
            source.setStatus(UpskillingSourceStatus.READY);
            source.setUpdatedAt(LocalDateTime.now());
            repository.save(source);

            return new SourceUploadResponse(
                    source.getId(),
                    source.getFileName(),
                    "READY",
                    summary
            );

        } catch (Exception e) {
            source.setStatus(UpskillingSourceStatus.FAILED);
            source.setErrorMessage(e.getMessage());
            source.setUpdatedAt(LocalDateTime.now());
            repository.save(source);
            throw new RuntimeException(e.getMessage());
        }
    }

    private String extractText(MultipartFile file) throws Exception {
        String contentType = file.getContentType();
        if (contentType != null && contentType.contains("pdf")) {
            try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                return new PDFTextStripper().getText(doc);
            }
        }
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    public SourceStatusResponse getSourceStatus(Long id) {
        UpskillingSource s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Source not found: " + id));

        return new SourceStatusResponse(
                s.getId(),
                s.getFileName(),
                s.getStatus().name(),
                s.getSummary(),
                100,
                s.getErrorMessage()
        );
    }

    public UpskillingSource getReady(Long id) {
        UpskillingSource s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Source not found: " + id));
        if (s.getStatus() != UpskillingSourceStatus.READY)
            throw new RuntimeException("Source not ready yet, status: " + s.getStatus());
        return s;
    }
}