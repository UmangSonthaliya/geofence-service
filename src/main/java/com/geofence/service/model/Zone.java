package com.geofence.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zone {
    private String zoneId;
    private Coordinate[] polygon;
}
