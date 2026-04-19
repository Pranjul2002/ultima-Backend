package com.edutech.edutechbackend.upskilling.service;

import com.edutech.edutechbackend.upskilling.dto.*;
import com.edutech.edutechbackend.upskilling.entity.UpskillingSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpskillingChatService {

    // Groq's context window is ~32k tokens; we clip at 24000 chars to stay safe
    private static final int MAX_CONTEXT_CHARS = 24000;

    private final UpskillingSourceService sourceService;
    private final GroqClientService groq;

    public AskQuestionResponse askQuestion(AskQuestionRequest req) {

        UpskillingSource source = sourceService.getReady(req.getSourceId());

        String fullText = source.getExtractedText();
        if (fullText == null || fullText.isBlank()) {
            return new AskQuestionResponse(
                    "The uploaded file has no readable text content.",
                    List.of()
            );
        }

        // Clip to fit Groq's context window
        String context = fullText.length() > MAX_CONTEXT_CHARS
                ? fullText.substring(0, MAX_CONTEXT_CHARS)
                : fullText;

        String answer = groq.generate(
                """
                You are a helpful assistant that answers questions based on the provided document content.
                Answer using ONLY the information in the context below.
                If the answer is not present in the context, respond with exactly:
                "I could not find that information in the uploaded file."
                Be concise, accurate, and do not make up information.
                """,
                "Document content:\n" + context + "\n\n---\nQuestion: " + req.getQuestion()
        );

        String snippet = context.length() > 300 ? context.substring(0, 300) + "..." : context;
        List<CitationResponse> citations = List.of(new CitationResponse(null, snippet));

        return new AskQuestionResponse(answer, citations);
    }
}