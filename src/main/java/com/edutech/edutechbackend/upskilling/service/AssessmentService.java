package com.edutech.edutechbackend.upskilling.service;

import com.edutech.edutechbackend.upskilling.dto.*;
import com.edutech.edutechbackend.upskilling.entity.UpskillingSource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentService {

    // Clip document context so we stay well within Groq's context window
    private static final int MAX_CONTEXT_CHARS = 18000;

    private final UpskillingSourceService sourceService;
    private final GroqClientService groq;
    private final ObjectMapper objectMapper;

    // ─────────────────────────────────────────────
    //  1. GENERATE QUESTIONS from a source PDF
    // ─────────────────────────────────────────────
    public GenerateQuestionsResponse generateQuestions(GenerateQuestionsRequest req) {

        UpskillingSource source = sourceService.getReady(req.getSourceId());

        String context = clipContext(source.getExtractedText());
        String topicHint = req.getTopic() != null && !req.getTopic().isBlank()
                ? " Focus specifically on the topic: " + req.getTopic() + "."
                : "";

        String systemPrompt = """
                You are an expert educator. Generate exam-style questions ONLY based on the
                provided document content. Do NOT make up questions outside the document.
                Return ONLY a valid JSON array — no markdown, no backticks, no extra text.
                Each element must be: {"number": N, "question": "...", "type": "short_answer"}
                """;

        String userPrompt = "Document content:\n" + context
                + "\n\nGenerate exactly " + req.getCount() + " varied exam questions "
                + "covering different parts of this document." + topicHint
                + "\nReturn ONLY the JSON array.";

        String raw = groq.generate(systemPrompt, userPrompt);

        List<GeneratedQuestion> questions = parseQuestionsJson(raw, req.getCount());

        return GenerateQuestionsResponse.builder()
                .sourceId(source.getId())
                .sourceFileName(source.getFileName())
                .questions(questions)
                .build();
    }

    // ─────────────────────────────────────────────
    //  2. EVALUATE student answers
    // ─────────────────────────────────────────────
    public EvaluationResult evaluateAnswers(EvaluateAnswersRequest req) {

        UpskillingSource source = sourceService.getReady(req.getSourceId());
        String context = clipContext(source.getExtractedText());

        List<EvaluationResult.QuestionResult> results = new ArrayList<>();

        for (EvaluateAnswersRequest.StudentAnswer sa : req.getAnswers()) {

            // Step 1 — resolve answer text
            // If the student uploaded a handwritten PDF/image, extract text from it first
            String answerText = resolveAnswerText(sa);

            // Step 2 — ask Groq to evaluate this single answer against the document
            String systemPrompt = """
                    You are a strict but fair examiner. Evaluate the student's answer ONLY
                    against the provided document content. Do NOT use outside knowledge.
                    Return ONLY a valid JSON object — no markdown, no backticks:
                    {
                      "correct": true/false,
                      "feedback": "concise explanation of why correct or wrong (2-3 sentences)",
                      "correctAnswer": "brief correct answer based on the document (1-2 sentences)"
                    }
                    """;

            String userPrompt = "Document content:\n" + context
                    + "\n\nQuestion: " + sa.getQuestionText()
                    + "\n\nStudent answer: " + answerText
                    + "\n\nEvaluate and return only the JSON object.";

            String raw = groq.generate(systemPrompt, userPrompt);
            Map<String, Object> eval = parseEvalJson(raw);

            results.add(EvaluationResult.QuestionResult.builder()
                    .questionNumber(sa.getQuestionNumber())
                    .questionText(sa.getQuestionText())
                    .studentAnswer(answerText)
                    .correct(Boolean.TRUE.equals(eval.get("correct")))
                    .feedback(String.valueOf(eval.getOrDefault("feedback", "")))
                    .correctAnswer(String.valueOf(eval.getOrDefault("correctAnswer", "")))
                    .build());
        }

        long correctCount = results.stream().filter(EvaluationResult.QuestionResult::isCorrect).count();
        int total = results.size();
        int score = total > 0 ? (int) Math.round((double) correctCount / total * 100) : 0;

        return EvaluationResult.builder()
                .sourceId(source.getId())
                .totalQuestions(total)
                .correctCount((int) correctCount)
                .wrongCount(total - (int) correctCount)
                .score(score)
                .results(results)
                .build();
    }

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────

    /**
     * If the student uploaded a handwritten PDF or image, extract its text using PDFBox.
     * For images we send the raw bytes to Groq's vision endpoint (or fall back to
     * a placeholder if vision is unavailable). For plain text answers just return as-is.
     */
    private String resolveAnswerText(EvaluateAnswersRequest.StudentAnswer sa) {
        if (sa.getTextAnswer() != null && !sa.getTextAnswer().isBlank()) {
            return sa.getTextAnswer().trim();
        }

        if (sa.getHandwrittenBase64() == null || sa.getHandwrittenBase64().isBlank()) {
            return "[No answer provided]";
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(sa.getHandwrittenBase64());
            String mime = sa.getHandwrittenMimeType();

            if (mime != null && mime.contains("pdf")) {
                // Use PDFBox to extract text from a typed/digital PDF
                try (PDDocument doc = Loader.loadPDF(bytes)) {
                    String extracted = new PDFTextStripper().getText(doc).trim();
                    return extracted.isBlank() ? transcribeWithGroq(bytes, mime) : extracted;
                }
            }

            // Image (JPEG / PNG) — use Groq vision to transcribe handwriting
            return transcribeWithGroq(bytes, mime);

        } catch (Exception e) {
            log.warn("Failed to extract handwritten answer: {}", e.getMessage());
            return "[Could not read handwritten answer — " + e.getMessage() + "]";
        }
    }

    /**
     * Sends an image to Groq's vision-capable model to transcribe handwriting.
     * Groq supports vision via llama-3.2-11b-vision-preview.
     */
    private String transcribeWithGroq(byte[] imageBytes, String mimeType) {
        return groq.transcribeHandwriting(imageBytes, mimeType);
    }

    private String clipContext(String text) {
        if (text == null || text.isBlank()) return "";
        return text.length() > MAX_CONTEXT_CHARS
                ? text.substring(0, MAX_CONTEXT_CHARS)
                : text;
    }

    private List<GeneratedQuestion> parseQuestionsJson(String raw, int expectedCount) {
        try {
            // Strip possible markdown fences
            String cleaned = raw.replaceAll("(?s)```[a-z]*\\s*", "").replaceAll("```", "").trim();
            // Find the JSON array
            int start = cleaned.indexOf('[');
            int end = cleaned.lastIndexOf(']');
            if (start >= 0 && end > start) {
                cleaned = cleaned.substring(start, end + 1);
            }
            List<Map<String, Object>> raw2 = objectMapper.readValue(cleaned,
                    new TypeReference<>() {});
            List<GeneratedQuestion> list = new ArrayList<>();
            for (Map<String, Object> item : raw2) {
                list.add(GeneratedQuestion.builder()
                        .number(((Number) item.getOrDefault("number", list.size() + 1)).intValue())
                        .question(String.valueOf(item.getOrDefault("question", "")))
                        .type(String.valueOf(item.getOrDefault("type", "short_answer")))
                        .build());
            }
            return list;
        } catch (Exception e) {
            log.warn("Failed to parse questions JSON, falling back: {}", e.getMessage());
            // Return a single fallback question so the UI isn't broken
            return List.of(GeneratedQuestion.builder()
                    .number(1)
                    .question("Summarize the main topic of this document.")
                    .type("short_answer")
                    .build());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseEvalJson(String raw) {
        try {
            String cleaned = raw.replaceAll("(?s)```[a-z]*\\s*", "").replaceAll("```", "").trim();
            int start = cleaned.indexOf('{');
            int end = cleaned.lastIndexOf('}');
            if (start >= 0 && end > start) cleaned = cleaned.substring(start, end + 1);
            return objectMapper.readValue(cleaned, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse eval JSON: {}", e.getMessage());
            return Map.of(
                    "correct", false,
                    "feedback", "Could not evaluate this answer automatically.",
                    "correctAnswer", "Please refer to the source document."
            );
        }
    }
}