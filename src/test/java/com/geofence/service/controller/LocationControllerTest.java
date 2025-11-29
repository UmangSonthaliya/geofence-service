package com.geofence.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geofence.service.model.LocationEvent;
import com.geofence.service.model.VehicleState;
import com.geofence.service.model.ZoneTransitionEvent;
import com.geofence.service.service.GeofenceService;
import com.geofence.service.service.VehicleStateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GeofenceService geofenceService;

    @MockBean
    private VehicleStateService stateService;

    @Test
    void handleLocation_ValidEvent_ReturnsTransition() throws Exception {
        LocationEvent event = new LocationEvent("V001", 12.9350, 77.6650, 1000L);
        ZoneTransitionEvent transition = new ZoneTransitionEvent("V001", null, "bellandur", 1000L);

        when(geofenceService.process(any(LocationEvent.class))).thenReturn(transition);

        mockMvc.perform(post("/api/events/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value("V001"))
                .andExpect(jsonPath("$.toZone").value("bellandur"));
    }

    @Test
    void handleLocation_InvalidEvent_ReturnsBadRequest() throws Exception {
        LocationEvent event = new LocationEvent("", 200.0, 200.0, -1L);

        mockMvc.perform(post("/api/events/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getZone_ExistingVehicle_ReturnsState() throws Exception {
        VehicleState state = new VehicleState("V001", "bellandur", 1000L);
        when(stateService.getState("V001")).thenReturn(state);

        mockMvc.perform(get("/api/vehicles/V001/zone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value("V001"))
                .andExpect(jsonPath("$.currentZone").value("bellandur"));
    }

    @Test
    void getZone_NonExistentVehicle_ReturnsNotFound() throws Exception {
        when(stateService.getState("V999")).thenReturn(null);

        mockMvc.perform(get("/api/vehicles/V999/zone"))
                .andExpect(status().isNotFound());
    }
}

