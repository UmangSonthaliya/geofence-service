package com.geofence.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Health and status check endpoints")
public class HealthController {

    private final RedisTemplate<String, Object> redisTemplate;

    public HealthController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Operation(summary = "Health check", description = "Returns service health status")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toEpochMilli());
        response.put("service", "geofence-service");
        
        // Check Redis connectivity
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            response.put("redis", "connected");
        } catch (Exception e) {
            response.put("redis", "disconnected");
            response.put("status", "DEGRADED");
        }
        
        return ResponseEntity.ok(response);
    }
}

