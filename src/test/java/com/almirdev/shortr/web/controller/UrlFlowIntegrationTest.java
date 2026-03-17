package com.almirdev.shortr.web.controller;

import com.almirdev.shortr.TestcontainersConfiguration;
import com.almirdev.shortr.application.dto.ShortenRequest;
import com.almirdev.shortr.application.dto.ShortenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class UrlFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void shouldShortenAndThenRedirectSuccessfully() {
        // Step 1: Shorten
        String longUrl = "https://www.google.com";
        ShortenRequest request = new ShortenRequest(longUrl);

        ResponseEntity<ShortenResponse> shortenResponse = restTestClient.post()
                .uri("http://localhost:%d/shorten".formatted(port))
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ShortenResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(responseBody).isNotNull();
        String shortCode = responseBody.getShortCode();
        assertThat(shortCode).isNotBlank();

        // Step 2: Redirect
        restTestClient.get()
                .uri("http://localhost:%d/%s".formatted(port, shortCode))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", longUrl);
    }

    @Test
    void shouldReturn404ForNonExistentCode() {
        restTestClient.get()
                .uri("http://localhost:%d/nonexistent".formatted(port))
                .exchange()
                .expectStatus().isNotFound();
    }
}
