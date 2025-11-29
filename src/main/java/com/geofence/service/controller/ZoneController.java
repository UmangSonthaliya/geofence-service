package com.geofence.service.controller;

import com.geofence.service.dto.ZoneDTO;
import com.geofence.service.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/zones")
@Tag(name = "Zone Management", description = "APIs for managing and querying geofence zones")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @Operation(summary = "Get all zones", description = "Returns a list of all configured geofence zones")
    @GetMapping
    public ResponseEntity<List<ZoneDTO>> getAllZones() {
        log.debug("Fetching all zones");
        List<ZoneDTO> zones = zoneService.getAllZones();
        return ResponseEntity.ok(zones);
    }

    @Operation(summary = "Detect zone for coordinates", description = "Returns the zone ID for given lat/lon coordinates")
    @GetMapping("/detect")
    public ResponseEntity<Map<String, String>> detectZone(
            @RequestParam double lat,
            @RequestParam double lon) {
        
        log.debug("Detecting zone for lat={}, lon={}", lat, lon);
        String zoneId = zoneService.detectZone(lat, lon);
        
        Map<String, String> response = new HashMap<>();
        response.put("lat", String.valueOf(lat));
        response.put("lon", String.valueOf(lon));
        response.put("zoneId", zoneId != null ? zoneId : "outside_all_zones");
        
        return ResponseEntity.ok(response);
    }
}

