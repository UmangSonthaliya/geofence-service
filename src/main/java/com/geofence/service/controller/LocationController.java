package com.geofence.service.controller;

import com.geofence.service.exception.ResourceNotFoundException;
import com.geofence.service.model.LocationEvent;
import com.geofence.service.model.VehicleState;
import com.geofence.service.model.ZoneTransitionEvent;
import com.geofence.service.service.GeofenceService;
import com.geofence.service.service.VehicleStateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Location Tracking", description = "APIs for processing vehicle location events and zone transitions")
public class LocationController {

    private final GeofenceService geofenceService;
    private final VehicleStateService stateService;

    public LocationController(GeofenceService geofenceService, VehicleStateService stateService) {
        this.geofenceService = geofenceService;
        this.stateService = stateService;
    }

    @Operation(
            summary = "Process location event",
            description = "Accepts a vehicle location event and detects zone transitions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event processed successfully",
                    content = @Content(schema = @Schema(implementation = ZoneTransitionEvent.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/events/location")
    public ResponseEntity<?> handleLocation(
            @Valid @RequestBody LocationEvent event) {
        
        log.debug("Received location event for vehicle: {}", event.getVehicleId());
        
        ZoneTransitionEvent transition = geofenceService.process(event);
        
        if (transition != null) {
            log.info("Zone transition detected: {}", transition);
            return ResponseEntity.ok(transition);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "No zone change detected");
            response.put("vehicleId", event.getVehicleId());
            return ResponseEntity.ok(response);
        }
    }

    @Operation(
            summary = "Get vehicle zone status",
            description = "Returns the current zone and last updated timestamp for a vehicle"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle state found",
                    content = @Content(schema = @Schema(implementation = VehicleState.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/vehicles/{vehicleId}/zone")
    public ResponseEntity<VehicleState> getZone(
            @Parameter(description = "Vehicle ID to query") 
            @PathVariable String vehicleId) {
        
        log.debug("Querying zone for vehicle: {}", vehicleId);
        
        VehicleState state = stateService.getState(vehicleId);
        if (state == null) {
            throw new ResourceNotFoundException("Vehicle not found: " + vehicleId);
        }
        
        return ResponseEntity.ok(state);
    }
}
