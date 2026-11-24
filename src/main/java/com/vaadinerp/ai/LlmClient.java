package com.vaadinerp.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Klien tipis untuk server LLM lokal berformat OpenAI ({@code /v1/chat/completions}).
 * Dipakai AI Generator di Script Editor; chat akan memakai kelas yang sama.
 * Konfigurasi: app.ai.base-url, app.ai.model, app.ai.api-key (lewat env AI_API_KEY), app.ai.timeout-seconds.
 */
@Service
public class LlmClient {

    private final String baseUrl;
    private final String model;
    private final String apiKey;
    private final Duration timeout;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    public LlmClient(@Value("${app.ai.base-url:}") String baseUrl,
            @Value("${app.ai.model:}") String model,
            @Value("${app.ai.api-key:}") String apiKey,
            @Value("${app.ai.timeout-seconds:60}") int timeoutSeconds) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
        this.model = model == null ? "" : model.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    public String getModel() {
        return model;
    }

    /** Satu pertanyaan sekali jalan (system + user) -> teks jawaban. */
    public String ask(String system, String user) throws IOException, InterruptedException {
        if (baseUrl.isEmpty() || model.isEmpty()) {
            throw new IllegalStateException("AI is not configured (set app.ai.base-url and app.ai.model).");
        }
        String body = mapper.writeValueAsString(Map.of(
                "model", model,
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user))));

        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (!apiKey.isEmpty()) {
            req.header("Authorization", "Bearer " + apiKey);
        }

        HttpResponse<String> res = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IOException("AI server returned HTTP " + res.statusCode() + ": " + res.body());
        }
        JsonNode root = mapper.readTree(res.body());
        // reasoning_content (proses berpikir model) sengaja diabaikan; hanya content yang dipakai.
        String content = root.path("choices").path(0).path("message").path("content").asText("");
        return content.replaceAll("(?s)<think>.*?</think>", "").trim();
    }
}
