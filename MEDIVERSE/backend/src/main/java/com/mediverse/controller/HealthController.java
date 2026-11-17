package com.mediverse.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealthStatus() {
        Map<String, String> response = Map.of(
            "status", "UP",
            "service", "Mediverse Backend"
        );
        return ResponseEntity.ok(response);
    }
}
