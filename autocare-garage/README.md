# AutoCare Garage

A vehicle servicing platform built as three Spring Boot services. Written for a
Jenkins to Azure Kubernetes Service CI/CD project, replacing the usual
spring-petclinic demo.

## The three services

| Service | Port | Owns | Talks to |
|---|---|---|---|
| `customers-service` | 8081 | Customers, Vehicles | nothing |
| `workshop-service` | 8082 | Mechanics, Specialties, ServiceJobs | customers-service |
| `web-ui` | 8080 | Thymeleaf pages, no database | both backends |

Each backend has its own in-memory H2 database seeded on startup, so there is no
external database to run. `web-ui` holds no data at all.

The interesting bit is `workshop-service` calling `customers-service`: a service
job only stores a `vehicleId`, and the vehicle itself is fetched over HTTP at
request time. If `customers-service` is down, jobs still render, just without
vehicle details. That is deliberate.

## Requirements

- JDK 21
- Maven 3.9+
- Docker (only for the container route)

Check with `java -version` and `mvn -version`.

## Run it locally

### Option A - three terminals, no Docker

```bash
mvn clean package

# terminal 1
java -jar customers-service/target/customers-service-1.0.0.jar

# terminal 2
java -jar workshop-service/target/workshop-service-1.0.0.jar

# terminal 3
java -jar web-ui/target/web-ui-1.0.0.jar
```

Start customers-service first. Then open <http://localhost:8080>.

On Linux or macOS you can use `./run-local.sh` instead, which does all three.

### Option B - Docker Compose

```bash
mvn clean package
docker compose up --build
```

Then <http://localhost:8080>. Stop with `docker compose down`.

## Verify it works

```bash
# 1. Both backends respond
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health

# 2. Seed data loaded
curl http://localhost:8081/api/customers | head -c 400
curl http://localhost:8082/api/workshop/mechanics | head -c 400

# 3. The cross-service call works - jobs come back with a "vehicle" object
curl http://localhost:8082/api/workshop/jobs | head -c 600

# 4. Dashboard counters
curl http://localhost:8082/api/workshop/jobs/stats
```

In the browser, walk through:

1. **Dashboard** - counters and the two service health tiles.
2. **Job board** - filter by status; click a job.
3. **Job detail** - press a status button and watch the colour change.
4. **Customers** - click one; the service history at the bottom is joined across
   both services.
5. **Book a job** - the vehicle dropdown is populated from customers-service.
6. **Services** - live health of both backends.

### Prove the services really are independent

Stop `customers-service` (Ctrl-C in its terminal), then reload
<http://localhost:8080/jobs>. The jobs are still listed, but each vehicle shows
as `Vehicle #n (unavailable)`, and the Services page turns that tile red.
Start it again and the details come back.

This is the same failure you will demo in Kubernetes with `kubectl delete pod`.

## Run the tests

```bash
mvn clean test
```

Roughly 20 tests across the three modules: JPA repository tests, MockMvc
controller tests, and plain unit tests for the job state machine and the UI
formatting helpers. JaCoCo writes coverage to
`*/target/site/jacoco/jacoco.xml` for SonarQube to pick up later.

## API reference

### customers-service

```
GET    /api/customers
GET    /api/customers?lastName=sharma
GET    /api/customers/{id}
POST   /api/customers
PUT    /api/customers/{id}
DELETE /api/customers/{id}
GET    /api/customers/{id}/vehicles
POST   /api/customers/{id}/vehicles
GET    /api/vehicles
GET    /api/vehicles/{id}
```

### workshop-service

```
GET    /api/workshop/jobs
GET    /api/workshop/jobs?status=IN_PROGRESS
GET    /api/workshop/jobs?vehicleId=3
GET    /api/workshop/jobs/{id}
GET    /api/workshop/jobs/stats
POST   /api/workshop/jobs
PATCH  /api/workshop/jobs/{id}/status?status=COMPLETED
GET    /api/workshop/mechanics
GET    /api/workshop/mechanics/{id}
POST   /api/workshop/mechanics
GET    /api/workshop/specialties
```

Every service also exposes `/actuator/health`, `/actuator/health/liveness`,
`/actuator/health/readiness`, `/actuator/info` and `/actuator/metrics`.
The liveness and readiness endpoints are what the Kubernetes probes will use.

## Configuration

Service URLs are environment variables so the same JAR and the same image run
locally and in Kubernetes:

| Variable | Used by | Local default | In Kubernetes |
|---|---|---|---|
| `CUSTOMERS_SERVICE_URL` | workshop-service, web-ui | `http://localhost:8081` | `http://customers-service:8081` |
| `WORKSHOP_SERVICE_URL` | web-ui | `http://localhost:8082` | `http://workshop-service:8082` |

## Build the images

```bash
mvn clean package

docker build -t customers-service:1 ./customers-service
docker build -t workshop-service:1 ./workshop-service
docker build -t web-ui:1 ./web-ui
```

The Dockerfiles are deliberately thin - they copy an already-built JAR onto a
JRE base image. Maven runs once in the Jenkins pipeline, not three times inside
Docker.

## Layout

```
autocare-garage/
├── pom.xml                  parent POM, three modules, JaCoCo
├── customers-service/
├── workshop-service/
├── web-ui/
├── docker-compose.yml
└── run-local.sh
```

Kubernetes manifests, the Jenkinsfiles and the Azure setup come next.
