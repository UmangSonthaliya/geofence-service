package com.geofence.service.service;

import com.geofence.service.model.VehicleState;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class VehicleStateService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "vehicle:";

    public VehicleStateService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public VehicleState getState(String vehicleId) {
        return (VehicleState) redisTemplate.opsForValue().get(KEY_PREFIX + vehicleId);

    }

    public void saveState(VehicleState state) {
        redisTemplate.opsForValue().set(KEY_PREFIX + state.getVehicleId(), state);

    }
}
