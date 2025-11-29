package com.geofence.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Geofence Service");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toEpochMilli());
        response.put("documentation", "/swagger-ui.html");
        response.put("api", "/api");
        response.put("health", "/api/health");
        return ResponseEntity.ok(response);
    }
}

