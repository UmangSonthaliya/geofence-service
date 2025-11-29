package com.geofence.service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZoneServiceTest {

    private ZoneService zoneService;

    @BeforeEach
    void setUp() {
        zoneService = new ZoneService();
        zoneService.init();
    }

    @Test
    void detectZone_Bellandur_ReturnsCorrectZone() {
        String zone = zoneService.detectZone(12.9350, 77.6650);
        assertThat(zone).isEqualTo("bellandur");
    }

    @Test
    void detectZone_HSRLayout_ReturnsCorrectZone() {
        // Test a coordinate clearly in HSR Layout without overlap
        String zone = zoneService.detectZone(12.9050, 77.6400);
        assertThat(zone).isEqualTo("hsr_layout");
    }

    @Test
    void detectZone_MGRoad_ReturnsCorrectZone() {
        String zone = zoneService.detectZone(12.9750, 77.6100);
        assertThat(zone).isEqualTo("mg_road");
    }

    @Test
    void detectZone_ElectronicCity_ReturnsCorrectZone() {
        String zone = zoneService.detectZone(12.8700, 77.6850);
        assertThat(zone).isEqualTo("electronic_city");
    }

    @Test
    void detectZone_Airport_ReturnsCorrectZone() {
        String zone = zoneService.detectZone(13.2250, 77.7100);
        assertThat(zone).isEqualTo("airport");
    }

    @Test
    void detectZone_OutsideAllZones_ReturnsNull() {
        String zone = zoneService.detectZone(12.9900, 77.5400);
        assertThat(zone).isNull();
    }

    @Test
    void detectZone_InvalidCoordinates_ReturnsNull() {
        String zone = zoneService.detectZone(0.0, 0.0);
        assertThat(zone).isNull();
    }

    @Test
    void getAllZones_ReturnsAllConfiguredZones() {
        var zones = zoneService.getAllZones();
        assertThat(zones).hasSize(7);
        assertThat(zones).extracting("zoneId")
                .contains("airport", "electronic_city", "mg_road", "bellandur", 
                         "hsr_layout", "indiranagar", "koramangala");
    }
}

