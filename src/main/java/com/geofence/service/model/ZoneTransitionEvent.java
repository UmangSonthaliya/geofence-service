package com.geofence.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneTransitionEvent {
    private String vehicleId;
    private String fromZone;
    private String toZone;
    private long timestamp;
}
