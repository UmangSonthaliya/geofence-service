package com.geofence.service.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationEvent {
    
    @NotBlank(message = "Vehicle ID is required")
    @Size(min = 1, max = 50, message = "Vehicle ID must be between 1 and 50 characters")
    private String vehicleId;
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double lat;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double lon;
    
    @Positive(message = "Timestamp must be positive")
    private long timestamp;
}
