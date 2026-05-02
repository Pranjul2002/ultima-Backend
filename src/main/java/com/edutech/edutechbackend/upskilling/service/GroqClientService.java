package com.edutech.edutechbackend.upskilling.service;

import com.edutech.edutechbackend.upskilling.dto.AskQuestionRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

/**
 * Updated GroqClientService — adds transcribeHandwriting() for vision-based
 * handwritten answer extraction using llama-3.2-11b-vision-preview.
 *
 * DROP-IN REPLACEMENT for the existing GroqClientService.java
 */
@Service
@RequiredArgsConstructor
public class GroqClientService {

    private final GroqProperties props;
    private final ObjectMapper mapper;

    private final HttpClient client = HttpClient.newHttpClient();

    // ── Existing methods (unchanged) ──────────────────────────────────────────

    public String generate(String system, String user) {
        return generateWithHistory(system, List.of(), user);
    }

    public String generateWithHistory(
            String system,
            List<AskQuestionRequest.HistoryMessage> history,
            String newUserMessage
    ) {
        try {
            ArrayNode messages = mapper.createArrayNode();
            messages.add(msg("system", system));
            for (AskQuestionRequest.HistoryMessage turn : history) {
                String role = turn.getRole();
                String content = turn.getContent();
                if (content != null && !content.isBlank()
                        && (role.equals("user") || role.equals("assistant"))) {
                    messages.add(msg(role, content));
                }
            }
            messages.add(msg("user", newUserMessage));

            return callGroq(props.getModel(), messages);

        } catch (Exception e) {
            throw new RuntimeException("Groq call failed: " + e.getMessage(), e);
        }
    }

    // ── NEW: Vision / handwriting transcription ───────────────────────────────

    /**
     * Sends an image (JPEG/PNG) to Groq's vision model and asks it to transcribe
     * any handwritten text it finds.
     *
     * Vision model: llama-3.2-11b-vision-preview
     * Falls back to a placeholder string on failure.
     */
    public String transcribeHandwriting(byte[] imageBytes, String mimeType) {
        try {
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String dataUri = "data:" + mimeType + ";base64," + base64;

            // Build a user message with an image_url content block
            ObjectNode imageBlock = mapper.createObjectNode();
            imageBlock.put("type", "image_url");
            ObjectNode imageUrl = mapper.createObjectNode();
            imageUrl.put("url", dataUri);
            imageBlock.set("image_url", imageUrl);

            ObjectNode textBlock = mapper.createObjectNode();
            textBlock.put("type", "text");
            textBlock.put("text",
                    "Please transcribe all the handwritten text in this image exactly as written. " +
                            "Return only the transcribed text, nothing else.");

            ArrayNode contentArray = mapper.createArrayNode();
            contentArray.add(textBlock);
            contentArray.add(imageBlock);

            ObjectNode userMessage = mapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.set("content", contentArray);

            ArrayNode messages = mapper.createArrayNode();
            // System message for context
            messages.add(msg("system",
                    "You are an OCR assistant. Transcribe handwritten text from images accurately."));
            messages.add(userMessage);

            // Use vision model
            return callGroq("llama-3.2-11b-vision-preview", messages);

        } catch (Exception e) {
            return "[Handwriting transcription failed: " + e.getMessage() + "]";
        }
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private String callGroq(String model, ArrayNode messages) throws Exception {
        String body = mapper.createObjectNode()
                .put("model", model)
                .set("messages", messages)
                .toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(props.getBaseUrl() + "/v1/chat/completions"))
                .header("Authorization", "Bearer " + props.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            System.err.println("Groq error " + res.statusCode() + ": " + res.body());
            throw new RuntimeException("Groq API call failed: " + res.statusCode());
        }

        JsonNode root = mapper.readTree(res.body());
        return root.path("choices").get(0)
                .path("message").path("content").asText();
    }

    private JsonNode msg(String role, String content) {
        return mapper.createObjectNode()
                .put("role", role)
                .put("content", content);
    }
}