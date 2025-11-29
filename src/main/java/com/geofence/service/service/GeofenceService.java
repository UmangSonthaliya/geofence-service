package com.geofence.service.service;

import com.geofence.service.model.LocationEvent;
import com.geofence.service.model.VehicleState;
import com.geofence.service.model.ZoneTransitionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class GeofenceService {

    private final ZoneService zoneService;
    private final VehicleStateService stateService;

    public GeofenceService(ZoneService zoneService, VehicleStateService stateService) {
        this.zoneService = zoneService;
        this.stateService = stateService;
    }

    public ZoneTransitionEvent process(LocationEvent event) {

        // Detect new zone
        String newZone = zoneService.detectZone(event.getLat(), event.getLon());

        // Get previous state
        VehicleState prev = stateService.getState(event.getVehicleId());
        if (prev == null) {
            prev = new VehicleState(event.getVehicleId(), null, 0);
        }

        // If zone changed → transition event
        if (!Objects.equals(prev.getCurrentZone(), newZone)) {

            // 🔥 LOG TRANSITION
            log.info("Vehicle {} transitioned from {} → {} at timestamp {}",
                    event.getVehicleId(),
                    prev.getCurrentZone(),
                    newZone,
                    event.getTimestamp());

            ZoneTransitionEvent transition = new ZoneTransitionEvent(
                    event.getVehicleId(),
                    prev.getCurrentZone(),
                    newZone,
                    event.getTimestamp()
            );

            // update state
            prev.setCurrentZone(newZone);
            prev.setLastUpdated(event.getTimestamp());
            stateService.saveState(prev);

            return transition;
        }

        // If same zone → update lastUpdated only
        prev.setLastUpdated(event.getTimestamp());
        stateService.saveState(prev);

        // Optional debug log
        log.debug("Vehicle {} stayed in zone {} at timestamp {}",
                event.getVehicleId(),
                newZone,
                event.getTimestamp());

        return null;
    }
}
