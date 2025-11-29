package com.geofence.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleState implements Serializable {

    private String vehicleId;
    private String currentZone;
    private long lastUpdated;
}
