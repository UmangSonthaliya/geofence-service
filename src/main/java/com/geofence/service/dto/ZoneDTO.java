package com.geofence.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneDTO {
    private String zoneId;
    private List<CoordinateDTO> polygon;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinateDTO {
        private double lat;
        private double lon;
    }
}

