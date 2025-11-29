#!/bin/bash

# Geofence Test Script
URL="http://localhost:8080/api/events/location"
VEHICLE="VTEST"

GREEN="\033[0;32m"
BLUE="\033[0;34m"
YELLOW="\033[1;33m"
RESET="\033[0m"

send_event() {
  ZONE_NAME=$1
  LAT=$2
  LON=$3
  TS=$4

  echo -e "${BLUE}➡ Testing Zone: ${YELLOW}$ZONE_NAME${RESET}"
  echo -e "   lat=$LAT, lon=$LON, timestamp=$TS"

  RESPONSE=$(curl -s -X POST "$URL" \
    -H "Content-Type: application/json" \
    -d "{
          \"vehicleId\": \"$VEHICLE\",
          \"lat\": $LAT,
          \"lon\": $LON,
          \"timestamp\": $TS
        }")

  echo -e "${GREEN}Response:${RESET} $RESPONSE"
  echo ""
}

echo -e "${YELLOW}========== RUNNING GEOFENCE TESTS ==========${RESET}"

# 1. Bellandur
send_event "bellandur" 12.9350 77.6650 1000

# 2. HSR Layout
send_event "hsr_layout" 12.9150 77.6450 2000

# 3. MG Road
send_event "mg_road" 12.9750 77.6100 3000

# 4. Electronic City
send_event "electronic_city" 12.8700 77.6900 4000

# 5. Indiranagar
send_event "indiranagar" 12.9750 77.6450 5000

# 6. Koramangala
send_event "koramangala" 12.9300 77.6200 6000

# 7. Airport
send_event "airport" 13.2250 77.7100 7000

# 8. Outside all zones
send_event "outside" 12.9900 77.5400 8000

echo -e "${YELLOW}========== COMPLETED ALL TESTS ==========${RESET}"


