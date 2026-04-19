package com.edutech.edutechbackend.upskilling.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class GroqClientService {

    private final GroqProperties props;
    private final ObjectMapper mapper;

    private final HttpClient client = HttpClient.newHttpClient();

    public String generate(String system, String user) {
        try {
            String body = mapper.createObjectNode()
                    .put("model", props.getModel())
                    .set("messages", mapper.createArrayNode()
                            .add(msg("system", system))
                            .add(msg("user", user)))
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

        } catch (Exception e) {
            throw new RuntimeException("Groq call failed: " + e.getMessage(), e);
        }
    }

    private JsonNode msg(String role, String content) {
        return mapper.createObjectNode()
                .put("role", role)
                .put("content", content);
    }
}