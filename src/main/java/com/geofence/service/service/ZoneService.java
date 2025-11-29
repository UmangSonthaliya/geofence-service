package com.geofence.service.service;

import com.geofence.service.dto.ZoneDTO;
import com.geofence.service.model.Zone;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ZoneService {

    private final List<Zone> zones = new ArrayList<>();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @PostConstruct
    public void init() {
        log.info("Initializing geofence zones...");
        int initialSize = zones.size();
        // Kempegowda International Airport (BLR Airport)
        zones.add(new Zone("airport", new Coordinate[]{
                new Coordinate(77.6980, 13.2050),
                new Coordinate(77.7280, 13.2050),
                new Coordinate(77.7280, 13.2500),
                new Coordinate(77.6980, 13.2500),
                new Coordinate(77.6980, 13.2050)
        }));

        // Electronic City (Phase 1 & 2)
        zones.add(new Zone("electronic_city", new Coordinate[]{
                new Coordinate(77.6600, 12.8400),
                new Coordinate(77.7100, 12.8400),
                new Coordinate(77.7100, 12.9000),
                new Coordinate(77.6600, 12.9000),
                new Coordinate(77.6600, 12.8400)
        }));

        // MG Road / Brigade Road region
        zones.add(new Zone("mg_road", new Coordinate[]{
                new Coordinate(77.6000, 12.9600),
                new Coordinate(77.6200, 12.9600),
                new Coordinate(77.6200, 12.9900),
                new Coordinate(77.6000, 12.9900),
                new Coordinate(77.6000, 12.9600)
        }));

        // Bellandur / ORR
        zones.add(new Zone("bellandur", new Coordinate[]{
                new Coordinate(77.6400, 12.9100),
                new Coordinate(77.7000, 12.9100),
                new Coordinate(77.7000, 12.9600),
                new Coordinate(77.6400, 12.9600),
                new Coordinate(77.6400, 12.9100)
        }));

        // HSR Layout (Sectors 1–7)
        zones.add(new Zone("hsr_layout", new Coordinate[]{
                new Coordinate(77.6300, 12.8900),
                new Coordinate(77.6600, 12.8900),
                new Coordinate(77.6600, 12.9400),
                new Coordinate(77.6300, 12.9400),
                new Coordinate(77.6300, 12.8900)
        }));

        // Indiranagar (80ft Road / CMH Road)
        zones.add(new Zone("indiranagar", new Coordinate[]{
                new Coordinate(77.6300, 12.9600),
                new Coordinate(77.6600, 12.9600),
                new Coordinate(77.6600, 12.9900),
                new Coordinate(77.6300, 12.9900),
                new Coordinate(77.6300, 12.9600)
        }));

        // Koramangala (1st to 8th Block)
        zones.add(new Zone("koramangala", new Coordinate[]{
                new Coordinate(77.6000, 12.9100),
                new Coordinate(77.6400, 12.9100),
                new Coordinate(77.6400, 12.9500),
                new Coordinate(77.6000, 12.9500),
                new Coordinate(77.6000, 12.9100)
        }));
        
        log.info("Loaded {} geofence zones", zones.size());
    }

    public String detectZone(double lat, double lon) {
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat)); // JTS uses (x=lon,y=lat)

        for (Zone zone : zones) {
            Polygon polygon = geometryFactory.createPolygon(zone.getPolygon());
            if (polygon.contains(point) || polygon.touches(point)) {
                return zone.getZoneId();
            }
        }
        return null;
    }
    
    public List<ZoneDTO> getAllZones() {
        return zones.stream()
                .map(zone -> {
                    List<ZoneDTO.CoordinateDTO> coordinates = new ArrayList<>();
                    for (Coordinate coord : zone.getPolygon()) {
                        coordinates.add(new ZoneDTO.CoordinateDTO(coord.y, coord.x));
                    }
                    return new ZoneDTO(zone.getZoneId(), coordinates);
                })
                .collect(Collectors.toList());
    }
}
