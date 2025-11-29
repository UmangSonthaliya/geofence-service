package com.geofence.service.service;

import com.geofence.service.model.LocationEvent;
import com.geofence.service.model.VehicleState;
import com.geofence.service.model.ZoneTransitionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeofenceServiceTest {

    @Mock
    private ZoneService zoneService;

    @Mock
    private VehicleStateService stateService;

    @InjectMocks
    private GeofenceService geofenceService;

    private LocationEvent locationEvent;

    @BeforeEach
    void setUp() {
        locationEvent = new LocationEvent("V001", 12.9350, 77.6650, 1000L);
    }

    @Test
    void process_FirstEvent_CreatesTransition() {
        // Arrange
        when(zoneService.detectZone(12.9350, 77.6650)).thenReturn("bellandur");
        when(stateService.getState("V001")).thenReturn(null);

        // Act
        ZoneTransitionEvent result = geofenceService.process(locationEvent);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getVehicleId()).isEqualTo("V001");
        assertThat(result.getFromZone()).isNull();
        assertThat(result.getToZone()).isEqualTo("bellandur");
        assertThat(result.getTimestamp()).isEqualTo(1000L);

        verify(stateService).saveState(any(VehicleState.class));
    }

    @Test
    void process_SameZone_NoTransition() {
        // Arrange
        VehicleState existingState = new VehicleState("V001", "bellandur", 500L);
        when(zoneService.detectZone(12.9350, 77.6650)).thenReturn("bellandur");
        when(stateService.getState("V001")).thenReturn(existingState);

        // Act
        ZoneTransitionEvent result = geofenceService.process(locationEvent);

        // Assert
        assertThat(result).isNull();
        verify(stateService).saveState(any(VehicleState.class));
    }

    @Test
    void process_ZoneChange_CreatesTransition() {
        // Arrange
        VehicleState existingState = new VehicleState("V001", "bellandur", 500L);
        when(zoneService.detectZone(12.9750, 77.6100)).thenReturn("mg_road");
        when(stateService.getState("V001")).thenReturn(existingState);

        LocationEvent newEvent = new LocationEvent("V001", 12.9750, 77.6100, 2000L);

        // Act
        ZoneTransitionEvent result = geofenceService.process(newEvent);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFromZone()).isEqualTo("bellandur");
        assertThat(result.getToZone()).isEqualTo("mg_road");
        verify(stateService).saveState(any(VehicleState.class));
    }

    @Test
    void process_ExitToOutside_CreatesTransition() {
        // Arrange
        VehicleState existingState = new VehicleState("V001", "bellandur", 500L);
        when(zoneService.detectZone(12.9900, 77.5400)).thenReturn(null);
        when(stateService.getState("V001")).thenReturn(existingState);

        LocationEvent outsideEvent = new LocationEvent("V001", 12.9900, 77.5400, 3000L);

        // Act
        ZoneTransitionEvent result = geofenceService.process(outsideEvent);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFromZone()).isEqualTo("bellandur");
        assertThat(result.getToZone()).isNull();
    }
}

