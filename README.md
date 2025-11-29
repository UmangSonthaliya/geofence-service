# Geofence Service

A high-performance, real-time geofencing service for vehicle location tracking built with Spring Boot 3.3. This service processes GPS location events, detects zone transitions using accurate polygon geofencing, and maintains vehicle state persistence with Redis.

![Java 21](https://img.shields.io/badge/Java-21-orange)
![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3-green)
![Redis](https://img.shields.io/badge/Redis-7-red)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)

## 🎯 Features

- **Real-time Location Processing**: Handle incoming GPS location events via REST API
- **Geofence Detection**: Automatic detection of zone entry/exit using JTS polygon operations
- **State Management**: Fast vehicle state persistence using Redis
- **Zone Transition Events**: Generate events when vehicles cross zone boundaries
- **Multiple Zones**: Pre-configured zones for Bangalore locations (Airport, Electronic City, MG Road, Bellandur, HSR Layout, Indiranagar, Koramangala)
- **API Documentation**: Interactive Swagger UI for API exploration
- **Health Checks**: Built-in health monitoring and metrics
- **Production-Ready**: Comprehensive error handling, logging, and monitoring via Actuator

## 🏗️ Architecture

### High-Level Design

```
┌─────────────┐
│   Vehicle   │
│   (Client)  │
└──────┬──────┘
       │ GPS Events (lat, lon, timestamp)
       │
       ▼
┌─────────────────────────────────────┐
│     Location Controller (REST)      │
│   POST /api/events/location         │
└──────────────┬──────────────────────┘
               │
               ▼
        ┌──────────────┐
        │  Geofence    │
        │  Service     │
        └──┬────────┬──┘
           │        │
           │        └──────────────┐
           │                       │
           ▼                       ▼
    ┌─────────────┐        ┌─────────────┐
    │    Zone     │        │   Vehicle   │
    │   Service   │        │   State     │
    │ (JTS Poly)  │        │  Service    │
    └─────────────┘        └──────┬──────┘
                                  │
                                  ▼
                           ┌──────────────┐
                           │    Redis     │
                           │ (State Store)│
                           └──────────────┘
```

### Technology Stack

- **Java 21**: Latest LTS version
- **Spring Boot 3.3.5**: Modern, stable Spring framework
- **SpringDoc OpenAPI 2.6.0**: Swagger UI and API documentation
- **Redis**: In-memory state storage for sub-millisecond lookups
- **JTS (Java Topology Suite)**: Robust polygon geometry calculations
- **Lombok**: Reduce boilerplate code
- **Micrometer**: Metrics and monitoring
- **Docker**: Containerization and orchestration

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose (Recommended)
- OR Java 21+ and Redis 7+ installed locally

### Option 1: Run with Docker Compose (Recommended)

This is the easiest way to get started. It spins up both the application and Redis.

```bash
# Build and start services
docker-compose up --build -d

# Check logs
docker-compose logs -f geofence-service
```

The service will be available at:
- **Root**: http://localhost:8080/
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

### Option 2: Run Locally

1. **Start Redis**
   ```bash
   docker run -d -p 6379:6379 redis:7-alpine
   ```

2. **Build and Run**
   ```bash
   mvn spring-boot:run
   ```

## 📚 API Documentation

The service exposes a fully documented REST API.

### Interactive Documentation
Visit **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)** to test endpoints directly in your browser.

### Key Endpoints

#### 1. Process Location Event
**POST** `/api/events/location`

Accepts a vehicle's GPS location and processes it for zone transitions.

```json
{
  "vehicleId": "V001",
  "lat": 12.9350,
  "lon": 77.6650,
  "timestamp": 1701234567890
}
```

#### 2. Get Vehicle Zone Status
**GET** `/api/vehicles/{vehicleId}/zone`

Returns the current zone and last updated timestamp for a vehicle.

#### 3. List All Zones
**GET** `/api/zones`

Returns a list of all configured geofence zones and their polygon coordinates.

#### 4. Detect Zone
**GET** `/api/zones/detect?lat={lat}&lon={lon}`

Helper endpoint to check which zone a specific coordinate falls into.

#### 5. Health & Metrics
- **Health**: `/api/health` (Application status)
- **Actuator Health**: `/actuator/health` (Detailed component status)
- **Metrics**: `/actuator/metrics` (Application metrics)

## 🧪 Testing

### Automated Tests
Run unit and integration tests:
```bash
mvn test
```

### Performance Testing Script
A custom script is included to simulate load:
```bash
chmod +x performance_test.sh
./performance_test.sh
```

## 🗺️ Configured Zones

The service comes pre-configured with 7 zones in Bangalore:

| Zone ID | Description |
|---------|-------------|
| `airport` | Kempegowda International Airport |
| `electronic_city` | Electronic City Phase 1 & 2 |
| `mg_road` | MG Road / Brigade Road |
| `bellandur` | Bellandur / ORR |
| `hsr_layout` | HSR Layout |
| `indiranagar` | Indiranagar |
| `koramangala` | Koramangala |

## 🔧 Configuration

The application is configured via `src/main/resources/application.yaml`.

**Key Settings:**
- `server.port`: 8080
- `spring.data.redis.host`: Redis host (default: localhost)
- `spring.data.redis.port`: Redis port (default: 6379)
- `springdoc.api-docs.path`: `/v3/api-docs`

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

MIT License
