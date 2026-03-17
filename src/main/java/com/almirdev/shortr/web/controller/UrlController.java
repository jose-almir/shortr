package com.almirdev.shortr.web.controller;

import com.almirdev.shortr.application.dto.ShortenRequest;
import com.almirdev.shortr.application.dto.ShortenResponse;
import com.almirdev.shortr.domain.service.UrlRedirectService;
import com.almirdev.shortr.domain.service.UrlShorteningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlShorteningService shorteningService;
    private final UrlRedirectService redirectService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        String shortCode = shorteningService.shorten(request.getLongUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ShortenResponse(shortCode));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String longUrl = redirectService.redirect(shortCode);
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(longUrl))
                .build();
    }
}
