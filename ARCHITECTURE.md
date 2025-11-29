# Architecture Documentation

## System Overview

The Geofence Service is designed as a lightweight, high-performance microservice for real-time vehicle location tracking and zone transition detection.

## Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Layer (REST)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │  Location    │  │    Zone      │  │   Health     │         │
│  │  Controller  │  │  Controller  │  │  Controller  │         │
│  └──────┬───────┘  └──────┬───────┘  └──────────────┘         │
└─────────┼──────────────────┼──────────────────────────────────┘
          │                  │
          │                  │
┌─────────┼──────────────────┼──────────────────────────────────┐
│         │    Service Layer │                                   │
│         ▼                  ▼                                   │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐      │
│  │  Geofence    │──▶│    Zone      │   │   Vehicle    │      │
│  │   Service    │   │   Service    │   │    State     │      │
│  │              │   │              │   │   Service    │      │
│  │ • Process    │   │ • Detect     │   │ • Get State  │      │
│  │   Events     │   │   Zone       │   │ • Save State │      │
│  │ • Detect     │   │ • List       │   │              │      │
│  │   Transition │   │   Zones      │   │              │      │
│  └──────────────┘   └──────────────┘   └──────┬───────┘      │
└──────────────────────────────────────────────────┼────────────┘
                                                   │
                                                   │
┌──────────────────────────────────────────────────┼────────────┐
│                   Data Layer                     │            │
│                                                  ▼            │
│                                         ┌──────────────┐      │
│                                         │    Redis     │      │
│                                         │              │      │
│                                         │ Key: vehicle:│      │
│                                         │      {id}    │      │
│                                         │ Value:       │      │
│                                         │  VehicleState│      │
│                                         └──────────────┘      │
└───────────────────────────────────────────────────────────────┘
```

## Request Flow

### Location Event Processing

```
1. Client sends GPS location event
   POST /api/events/location
   {
     "vehicleId": "V001",
     "lat": 12.9350,
     "lon": 77.6650,
     "timestamp": 1701234567890
   }

2. LocationController validates input
   - @Valid annotation triggers Bean Validation
   - Checks: vehicleId not blank, lat/lon in valid range

3. GeofenceService.process(event)
   a. Get current zone for coordinates
      └─▶ ZoneService.detectZone(lat, lon)
          └─▶ For each zone:
              - Create JTS Point
              - Create JTS Polygon from zone coordinates
              - Check if point is inside or touches polygon
              - Return first matching zone (or null)
   
   b. Get previous vehicle state
      └─▶ VehicleStateService.getState(vehicleId)
          └─▶ Redis GET vehicle:{vehicleId}
   
   c. Compare zones
      - If different → Create ZoneTransitionEvent
      - If same → Update timestamp only
   
   d. Save updated state
      └─▶ VehicleStateService.saveState(state)
          └─▶ Redis SET vehicle:{vehicleId}

4. Return response
   - ZoneTransitionEvent if zone changed
   - "No zone change" message otherwise
```

## Data Models

### LocationEvent (Input)
```java
{
  vehicleId: String      // Unique vehicle identifier
  lat: Double           // Latitude (-90 to 90)
  lon: Double           // Longitude (-180 to 180)
  timestamp: Long       // Unix timestamp in milliseconds
}
```

### VehicleState (Redis)
```java
{
  vehicleId: String      // Vehicle identifier
  currentZone: String    // Current zone ID (or null if outside)
  lastUpdated: Long      // Last event timestamp
}
```

### ZoneTransitionEvent (Output)
```java
{
  vehicleId: String      // Vehicle identifier
  fromZone: String       // Previous zone (null if first event)
  toZone: String         // New zone (null if exited all zones)
  timestamp: Long        // Event timestamp
}
```

### Zone (Configuration)
```java
{
  zoneId: String                // Unique zone identifier
  polygon: Coordinate[]         // Array of lat/lon coordinates
                               // First and last must be identical (closed polygon)
}
```

## State Management

### Redis Key Structure

```
Pattern: vehicle:{vehicleId}
Example: vehicle:V001

Value: Serialized VehicleState object (JSON)
{
  "vehicleId": "V001",
  "currentZone": "bellandur",
  "lastUpdated": 1701234567890
}

TTL: None (persists until explicitly deleted)
```

### State Transitions

```
State Diagram:

   [Initial]
      │
      ▼
   No State ──────▶ Zone A ◀──────┐
                     │  ▲          │
                     ▼  │          │
                   Zone B          │
                     │              │
                     ▼              │
                   Outside All Zones
```

Possible Transitions:
1. `null → Zone A`: First event, entering zone
2. `Zone A → Zone A`: Staying in same zone (no transition event)
3. `Zone A → Zone B`: Moving between zones (transition event)
4. `Zone A → null`: Exiting all zones (transition event)
5. `null → null`: Moving outside zones (no transition after first)

## Geospatial Algorithm

### Point-in-Polygon Detection

We use JTS (Java Topology Suite) which implements the **Ray Casting Algorithm**:

```
Algorithm:
1. Cast a ray from the point to infinity (typically horizontal)
2. Count how many times the ray crosses polygon edges
3. If odd number of crossings → inside
4. If even number of crossings → outside
5. Special case: if point is on edge → considered inside
```

**Time Complexity**: O(n) where n = number of polygon vertices  
**Space Complexity**: O(1)

### Zone Lookup Optimization

**Current Implementation** (Linear Search):
```java
for (Zone zone : zones) {
    if (polygon.contains(point)) return zone.getZoneId();
}
return null;
```
- **Complexity**: O(z × v) where z = zones, v = vertices per zone
- **Performance**: Fast for <100 zones

**Future Optimization** (Spatial Index):
```
Use R-tree or Quad-tree for O(log z) lookup:
1. Pre-build spatial index of zone bounding boxes
2. Query index with point coordinates
3. Only check detailed polygon for candidate zones
```

## Error Handling

### Exception Hierarchy

```
Exception
├── RuntimeException
    ├── ResourceNotFoundException
    │   └── Handled by: GlobalExceptionHandler
    │       Returns: 404 NOT FOUND
    │
    └── MethodArgumentNotValidException
        └── Handled by: GlobalExceptionHandler
            Returns: 400 BAD REQUEST with field errors
```

### Error Response Format

```json
{
  "timestamp": 1701234567890,
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input parameters",
  "path": "/api/events/location",
  "validationErrors": {
    "lat": "Latitude must be >= -90",
    "vehicleId": "Vehicle ID is required"
  }
}
```

## Performance Characteristics

### Latency Breakdown

```
Total Request Time: ~10ms
├── Network: ~2ms
├── Validation: ~0.5ms
├── Zone Detection: ~2ms
│   └── 7 zones × ~0.3ms per zone
├── Redis GET: ~1ms
├── Business Logic: ~0.5ms
├── Redis SET: ~1ms
└── Response Serialization: ~1ms
```

### Scalability Bottlenecks

1. **Zone Detection** (CPU-bound)
   - Linear search through all zones
   - Polygon calculations per zone
   - **Solution**: Spatial indexing

2. **Redis Connection** (I/O-bound)
   - Single connection can be bottleneck
   - **Solution**: Connection pooling (already implemented)

3. **Synchronous Processing** (Concurrency-bound)
   - Thread-per-request model
   - **Solution**: Migrate to reactive (WebFlux)

### Horizontal Scaling

```
Load Balancer
     │
     ├──▶ Instance 1 ──┐
     ├──▶ Instance 2 ──┼──▶ Redis Cluster
     └──▶ Instance 3 ──┘

Stateless Design Benefits:
✓ Any instance can handle any request
✓ No sticky sessions needed
✓ Easy to add/remove instances
✓ State is centralized in Redis
```

## Security Considerations

### Current Implementation
- Input validation prevents injection attacks
- Error messages don't leak sensitive info
- Health endpoints expose minimal information

### Production Additions Needed
- [ ] API authentication (JWT/API keys)
- [ ] Rate limiting per vehicle/client
- [ ] HTTPS/TLS encryption
- [ ] IP whitelisting for admin endpoints
- [ ] Audit logging for compliance

## Monitoring & Observability

### Metrics (Micrometer/Prometheus)

**Business Metrics**:
- `geofence.transitions.total` - Total zone transitions
- `geofence.vehicles.active` - Active vehicles tracked
- `geofence.zones.entered{zone}` - Entries per zone

**Technical Metrics**:
- `http.server.requests` - Request rate, latency, errors
- `jvm.memory.used` - Heap usage
- `redis.commands` - Redis operation stats

### Logging Levels

- **ERROR**: Unexpected exceptions, system failures
- **WARN**: Validation failures, missing resources
- **INFO**: Zone transitions, service startup
- **DEBUG**: Every request, state changes
- **TRACE**: Detailed algorithm execution

### Health Checks

1. **Liveness**: Is the service running?
   - Checks: JVM alive, HTTP server responding
   
2. **Readiness**: Can it handle traffic?
   - Checks: Redis connectivity, dependencies ready

## Testing Strategy

### Test Pyramid

```
        ┌─────────┐
        │   E2E   │  (Manual: test_geofence.sh)
        └─────────┘
       ┌───────────┐
       │Integration│  (Spring Boot Test + Redis)
       └───────────┘
     ┌──────────────┐
     │     Unit     │  (JUnit + Mockito)
     └──────────────┘
```

### Test Coverage

- **Unit Tests**: 80%+ coverage
  - GeofenceService logic
  - ZoneService polygon detection
  - Validation rules

- **Integration Tests**: Key flows
  - API endpoints with mocked services
  - Redis operations

- **Performance Tests**: Load testing
  - Concurrent vehicles simulation
  - Throughput measurement

## Deployment Architecture

### Development
```
Developer Machine
├── Java Application (Port 8080)
└── Redis (Port 6379)
```

### Docker Compose
```
Docker Network
├── geofence-service (Port 8080)
│   └── Connects to: redis
└── redis (Port 6379)
    └── Volume: redis-data
```

### Production (Future)
```
Kubernetes Cluster
├── Ingress (HTTPS)
│   └── LoadBalancer
│       ├── geofence-service (Pods × 3)
│       │   └── Resources: 500m CPU, 512Mi RAM
│       └── Redis Sentinel (HA)
│           ├── Master
│           └── Replicas × 2
└── Prometheus + Grafana (Monitoring)
```

## Configuration Management

### Environment-Specific Config

**Local** (`application.yaml`):
```yaml
redis:
  host: localhost
logging:
  level: DEBUG
```

**Docker** (`application-docker.yaml`):
```yaml
redis:
  host: redis  # Docker service name
logging:
  level: INFO
```

**Production** (`application-prod.yaml`):
```yaml
redis:
  host: ${REDIS_CLUSTER_ENDPOINT}
  ssl: true
  password: ${REDIS_PASSWORD}
logging:
  level: WARN
```

## Future Enhancements

### Phase 1: Performance
- Implement spatial indexing (R-tree)
- Add Redis caching for hot vehicles
- Migrate to reactive stack (WebFlux)

### Phase 2: Features
- Dynamic zone management API
- Historical transition tracking
- Webhook notifications
- Multi-tenant support

### Phase 3: Enterprise
- Kafka integration for event streaming
- PostgreSQL for durable storage
- Distributed tracing (Jaeger)
- Multi-region deployment

---

*Last Updated: November 2025*

