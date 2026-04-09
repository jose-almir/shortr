package com.almirdev.shortr.web.controller;

import com.almirdev.shortr.application.dto.ShortenRequest;
import com.almirdev.shortr.application.dto.ShortenResponse;
import com.almirdev.shortr.domain.service.UrlRedirectService;
import com.almirdev.shortr.domain.service.UrlShorteningService;
import com.almirdev.shortr.infrastructure.ratelimit.RateLimit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.temporal.ChronoUnit;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlShorteningService shorteningService;
    private final UrlRedirectService redirectService;

    @RateLimit(capacity = 10, refill = 10, unit = ChronoUnit.MINUTES)
    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        String shortCode = shorteningService.shorten(request.getLongUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ShortenResponse(shortCode));
    }

    @RateLimit(capacity = 300, refill = 300, unit = ChronoUnit.MINUTES)
    @GetMapping("/{shortCode:[A-Za-z0-9]+}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String longUrl = redirectService.redirect(shortCode);
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(longUrl))
                .build();
    }
}
