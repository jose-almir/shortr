package com.almirdev.shortr.web.controller;

import com.almirdev.shortr.infrastructure.ratelimit.RateLimit;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.temporal.ChronoUnit;

@Controller
public class IndexController {

    @RateLimit(capacity = 120, refill = 120, unit = ChronoUnit.MINUTES)
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
