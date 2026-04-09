package com.almirdev.shortr.web.controller;

import com.almirdev.shortr.TestcontainersConfiguration;
import com.almirdev.shortr.application.dto.ShortenRequest;
import com.almirdev.shortr.application.dto.ShortenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = "app.rate-limit.enabled=true"
)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class RateLimitIntegrationTest {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String RATE_LIMIT_MESSAGE = "Too many requests. Please try again later.";

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRateLimitIndexEndpoint() throws Exception {
        String clientIp = "198.51.100.10";

        for (int i = 0; i < 120; i++) {
            HttpResponse<String> response = get("/", clientIp);
            assertThat(response.statusCode()).isEqualTo(200);
        }

        HttpResponse<String> rateLimitedResponse = findFirstRateLimitedIndexResponse(clientIp, 20);
        assertThat(rateLimitedResponse.body()).contains(RATE_LIMIT_MESSAGE);
    }

    @Test
    void shouldRateLimitShortenEndpoint() throws Exception {
        String clientIp = "198.51.100.11";

        for (int i = 0; i < 10; i++) {
            HttpResponse<String> response = postJson("/shorten", clientIp,
                    new ShortenRequest("https://example.com/resource-" + i));
            assertThat(response.statusCode()).isEqualTo(201);
        }

        HttpResponse<String> rateLimitedResponse = findFirstRateLimitedShortenResponse(clientIp, 5);
        assertThat(rateLimitedResponse.body()).contains(RATE_LIMIT_MESSAGE);
    }

    @Test
    void shouldRateLimitRedirectEndpoint() throws Exception {
        String creationIp = "198.51.100.12";
        String redirectIp = "198.51.100.13";
        String longUrl = "https://example.com/redirect-target";

        HttpResponse<String> shortenResponse = postJson("/shorten", creationIp, new ShortenRequest(longUrl));
        assertThat(shortenResponse.statusCode()).isEqualTo(201);

        ShortenResponse created = objectMapper.readValue(shortenResponse.body(), ShortenResponse.class);
        assertThat(created).isNotNull();
        assertThat(created.getShortCode()).isNotBlank();

        for (int i = 0; i < 300; i++) {
            HttpResponse<String> response = get("/" + created.getShortCode(), redirectIp);
            assertThat(response.statusCode()).isEqualTo(301);
            assertThat(response.headers().firstValue(HttpHeaders.LOCATION)).contains(longUrl);
        }

        HttpResponse<String> rateLimitedResponse = findFirstRateLimitedRedirectResponse(created.getShortCode(), redirectIp, 40);
        assertThat(rateLimitedResponse.body()).contains(RATE_LIMIT_MESSAGE);
    }

    private HttpResponse<String> findFirstRateLimitedIndexResponse(String clientIp, int overflowAttempts)
            throws IOException, InterruptedException {
        for (int i = 0; i < overflowAttempts; i++) {
            HttpResponse<String> response = get("/", clientIp);
            if (response.statusCode() == 429) {
                return response;
            }
        }
        throw new AssertionError("Expected GET / to become rate limited within " + overflowAttempts + " overflow attempts");
    }

    private HttpResponse<String> findFirstRateLimitedShortenResponse(String clientIp, int overflowAttempts)
            throws IOException, InterruptedException {
        for (int i = 0; i < overflowAttempts; i++) {
            HttpResponse<String> response = postJson("/shorten", clientIp,
                    new ShortenRequest("https://example.com/resource-overflow-" + i));
            if (response.statusCode() == 429) {
                return response;
            }
        }
        throw new AssertionError("Expected POST /shorten to become rate limited within " + overflowAttempts + " overflow attempts");
    }

    private HttpResponse<String> findFirstRateLimitedRedirectResponse(String shortCode, String clientIp, int overflowAttempts)
            throws IOException, InterruptedException {
        for (int i = 0; i < overflowAttempts; i++) {
            HttpResponse<String> response = get("/" + shortCode, clientIp);
            if (response.statusCode() == 429) {
                return response;
            }
        }
        throw new AssertionError("Expected GET /{shortCode} to become rate limited within " + overflowAttempts + " overflow attempts");
    }

    private HttpResponse<String> get(String path, String clientIp) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri(path))
                .header("X-Forwarded-For", clientIp)
                .GET()
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJson(String path, String clientIp, Object body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri(path))
                .header("X-Forwarded-For", clientIp)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI buildUri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
