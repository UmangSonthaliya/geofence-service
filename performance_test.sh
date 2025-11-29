#!/bin/bash

# Performance Test Script for Geofence Service
# This script simulates high load with multiple concurrent vehicles

URL="http://localhost:8080/api/events/location"
CONCURRENT_VEHICLES=50
EVENTS_PER_VEHICLE=20

GREEN="\033[0;32m"
BLUE="\033[0;34m"
YELLOW="\033[1;33m"
RED="\033[0;31m"
RESET="\033[0m"

echo -e "${YELLOW}========== GEOFENCE PERFORMANCE TEST ==========${RESET}"
echo -e "${BLUE}Configuration:${RESET}"
echo -e "  Concurrent Vehicles: $CONCURRENT_VEHICLES"
echo -e "  Events per Vehicle: $EVENTS_PER_VEHICLE"
echo -e "  Total Events: $((CONCURRENT_VEHICLES * EVENTS_PER_VEHICLE))"
echo ""

# Test coordinates across different zones
ZONES=(
    "12.9350:77.6650:bellandur"
    "12.9150:77.6450:hsr_layout"
    "12.9750:77.6100:mg_road"
    "12.8700:77.6900:electronic_city"
    "12.9750:77.6450:indiranagar"
    "12.9300:77.6200:koramangala"
    "13.2250:77.7100:airport"
)

send_events_for_vehicle() {
    VEHICLE_ID=$1
    SUCCESS=0
    FAILED=0
    
    for i in $(seq 1 $EVENTS_PER_VEHICLE); do
        # Pick random zone
        ZONE_IDX=$((RANDOM % ${#ZONES[@]}))
        COORDS=${ZONES[$ZONE_IDX]}
        LAT=$(echo $COORDS | cut -d: -f1)
        LON=$(echo $COORDS | cut -d: -f2)
        TS=$(($(date +%s%3N)))
        
        RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$URL" \
            -H "Content-Type: application/json" \
            -d "{
                \"vehicleId\": \"$VEHICLE_ID\",
                \"lat\": $LAT,
                \"lon\": $LON,
                \"timestamp\": $TS
            }" 2>/dev/null)
        
        HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
        
        if [ "$HTTP_CODE" == "200" ]; then
            ((SUCCESS++))
        else
            ((FAILED++))
        fi
        
        # Small delay to avoid overwhelming the service
        sleep 0.01
    done
    
    echo "$VEHICLE_ID:$SUCCESS:$FAILED"
}

export -f send_events_for_vehicle
export URL EVENTS_PER_VEHICLE ZONES

echo -e "${GREEN}Starting load test...${RESET}"
START_TIME=$(date +%s)

# Generate vehicle IDs and run in parallel
TOTAL_SUCCESS=0
TOTAL_FAILED=0

for i in $(seq 1 $CONCURRENT_VEHICLES); do
    VEHICLE_ID=$(printf "V%04d" $i)
    send_events_for_vehicle $VEHICLE_ID &
done

# Wait for all background jobs to complete
wait

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo -e "${YELLOW}========== TEST RESULTS ==========${RESET}"
echo -e "${GREEN}Duration: ${DURATION}s${RESET}"
echo -e "${GREEN}Total Events: $((CONCURRENT_VEHICLES * EVENTS_PER_VEHICLE))${RESET}"
echo -e "${GREEN}Throughput: $((CONCURRENT_VEHICLES * EVENTS_PER_VEHICLE / DURATION)) events/sec${RESET}"
echo ""
echo -e "${BLUE}Check metrics at: http://localhost:8080/actuator/metrics${RESET}"
echo -e "${YELLOW}==========================================${RESET}"

