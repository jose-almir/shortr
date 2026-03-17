package com.almirdev.shortr.web.controller;

import com.almirdev.shortr.application.dto.ShortenRequest;
import com.almirdev.shortr.domain.exception.UrlNotFoundException;
import com.almirdev.shortr.domain.service.UrlRedirectService;
import com.almirdev.shortr.domain.service.UrlShorteningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlShorteningService shorteningService;

    @MockitoBean
    private UrlRedirectService redirectService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn201WhenShortenUrlSuccessfully() throws Exception {
        String longUrl = "https://google.com";
        String shortCode = "abc12345";
        ShortenRequest request = new ShortenRequest(longUrl);

        when(shorteningService.shorten(longUrl)).thenReturn(shortCode);

        mockMvc.perform(post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value(shortCode));
    }

    @Test
    void shouldReturn400WhenUrlIsInvalid() throws Exception {
        ShortenRequest request = new ShortenRequest("not-a-url");

        mockMvc.perform(post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.longUrl").exists());
    }

    @Test
    void shouldReturn301WhenRedirectCodeExists() throws Exception {
        String shortCode = "abc12345";
        String longUrl = "https://google.com";

        when(redirectService.redirect(shortCode)).thenReturn(longUrl);

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", longUrl));
    }

    @Test
    void shouldReturn404WhenShortCodeNotFound() throws Exception {
        when(redirectService.redirect(anyString())).thenThrow(new UrlNotFoundException("Not found"));

        mockMvc.perform(get("/invalid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"));
    }

    @Test
    void shouldReturn500WhenUnexpectedExceptionOccurs() throws Exception {
        String longUrl = "https://google.com";
        ShortenRequest request = new ShortenRequest(longUrl);

        when(shorteningService.shorten(longUrl)).thenThrow(new RuntimeException("Something broke"));

        mockMvc.perform(post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("An internal server error occurred"));
    }
}
