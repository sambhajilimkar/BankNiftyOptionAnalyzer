package com.banknifty.controller;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health endpoints for BankNifty Option Analyzer.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController implements HealthIndicator {

    @GetMapping
    public Map<String, Object> status() {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("application", "BankNiftyOptionAnalyzer");
        response.put("timestamp", Instant.now().toString());
        response.put("version", "1.0.0");

        return response;
    }

    @Override
    public Health health() {
        return Health.up()
                .withDetail("application", "BankNiftyOptionAnalyzer")
                .withDetail("timestamp", Instant.now().toString())
                .build();
    }
}
