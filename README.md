# Swiggy System

A microservices-based food/ride delivery platform, built from scratch as a hands-on learning project covering system design, Spring Boot, Kubernetes, Helm, CI/CD, observability, and cloud integration.

---

## Architecture Overview

Five Spring Boot microservices, backed by MySQL, MongoDB, Redis, and Kafka, orchestrated with Kubernetes and Helm, fronted by a single API Gateway.

```
                        ┌─────────────┐
                        │ API Gateway │
                        └──────┬──────┘
        ┌───────────┬──────────┼──────────┬───────────┐
        ▼           ▼           ▼           ▼
  Order Service  Catalog    Assignment  Tracking
   (MySQL)       Service     Service     Service
                 (MongoDB)   (Redis)    (Kafka + Redis)
```

| Service | Port | Database | Responsibility |
|---|---|---|---|
| `order-service` | 8081 | MySQL | Checkout, transactions, idempotency, JWT auth, order lifecycle |
| `catalog-service` | 8082 | MongoDB | Restaurants, nested menu items, CDN-backed images |
| `assignment-service` | 8083 | Redis | Geospatial rider matching, atomic claiming (Lua script) |
| `tracking-service` | 8084 | Kafka + Redis | Rider location ingestion, live SSE tracking (Redis Pub/Sub, multi-pod safe) |
| `api-gateway` | 8080 | — | Single entry point, path-based routing to all services |

---

## Tech Stack

- **Language/Framework:** Java 21, Spring Boot 4.1.0
- **Databases:** MySQL 8.0, MongoDB 7.0, Redis 7.2
- **Messaging:** Apache Kafka 4.2.1
- **Service-to-service:** Spring Cloud OpenFeign
- **Gateway:** Spring Cloud Gateway (servlet/webmvc)
- **Auth:** JWT (access + refresh tokens)
- **Resilience:** Resilience4j (Circuit Breaker), Bucket4j (rate limiting)
- **Containerization:** Docker (multi-stage builds), Docker Compose
- **Orchestration:** Kubernetes, Helm
- **Deployment strategies:** Rolling updates, Blue-Green, Canary (Argo Rollouts)
- **Observability:** Prometheus, Grafana, Jaeger (OpenTelemetry), Logback
- **CI/CD:** GitHub Actions (self-hosted runners, parallel matrix builds)
- **Cloud:** AWS S3 + CloudFront (CDN for restaurant images)

---

## Project Structure

```
swiggy-system/
├── order-service/
├── catalog-service/
├── assignment-service/
├── tracking-service/
├── api-gateway/
├── swiggy-chart/              # Helm chart for the entire system
│   ├── Chart.yaml
│   ├── values.yaml
│   └── templates/
├── docker-compose.yml         # Local infra (MySQL, Mongo, Redis, Kafka, Kafbat UI)
├── .github/workflows/
│   └── deploy.yml             # CI/CD pipeline
└── pom.xml                    # Parent POM (multi-module, Spring Cloud BOM)
```

---

## Local Development Setup

### Prerequisites
- Java 21, Maven
- Docker Desktop with Kubernetes enabled
- Helm
- kubectl
- IntelliJ IDEA (or any IDE)

### 1. Start local infrastructure

```bash
docker-compose up -d
```

Brings up MySQL, MongoDB, Redis, Kafka, and Kafbat UI (Kafka management UI) on your local Docker network.

### 2. Run services locally (IntelliJ or CLI)

Each service can be run directly against local Docker infra using its default `application.yaml`, or built as an image using its `Dockerfile`.

### 3. Deploy to Kubernetes via Helm

```bash
helm install swiggy ./swiggy-chart
```

Deploys all infrastructure (MySQL, MongoDB, Redis, Kafka), monitoring stack (Prometheus, Grafana), and all 5 microservices.

```bash
kubectl get pods
kubectl get svc
```

### 4. Upgrade after changes

```bash
helm upgrade swiggy ./swiggy-chart
```

### 5. Pausing vs tearing down — read this before stopping the cluster

**⚠️ `helm uninstall` deletes PersistentVolumeClaims too, since our MySQL/MongoDB PVCs are defined directly inside the chart's templates — this is standard Helm behavior, not a bug, but it means your database data is genuinely gone afterward, not just paused.**

To temporarily stop everything **without losing data**:
```bash
kubectl scale deployment --all --replicas=0
```
Restore with:
```bash
kubectl scale deployment --all --replicas=1
```

Only use `helm uninstall` for a genuine full teardown, and take backups first if the data matters:
```bash
kubectl exec -it <mysql-pod> -- mysqldump -u swiggy_user -pswiggy_pass123 order_service_db > backup.sql
kubectl exec -it <mongo-pod> -- mongodump --archive=/tmp/backup.archive
```

---

## CI/CD

Push to `main` triggers `.github/workflows/deploy.yml`:

1. Builds all 5 service images in parallel (matrix strategy, self-hosted runners)
2. Tags each image uniquely (`v{run_number}`)
3. Updates `values.yaml` with new tags
4. Runs `helm upgrade`
5. Verifies rollout status for every service

**Requires:** self-hosted GitHub Actions runner(s) registered against this repo, since builds/deploys target your local Kubernetes cluster.

---

## Key Design Decisions

- **MySQL for orders/payments** — ACID transactions needed for financial correctness; **MongoDB for catalog** — variable schema across restaurants, read-heavy.
- **Redis Geo** for live rider location (hot path, self-healing/ephemeral by design) vs **Kafka** as the ingestion backbone (decouples producers from multiple consumers).
- **SSE over WebSocket** for live tracking — traffic is genuinely one-directional; **Redis Pub/Sub** solves the cross-pod stateful-connection problem when Tracking Service scales beyond one replica.
- **Atomic rider claiming** via Redis Lua script — prevents two orders from grabbing the same rider under concurrent requests.
- **Circuit Breaker on Order→Assignment calls** — implemented via a separate bean (`AssignmentServiceCaller`) to work around Spring AOP's self-invocation limitation.
- **Blue-Green and Canary both implemented** — Blue-Green via dual Deployments + Service selector switching (plain Kubernetes); Canary via Argo Rollouts (genuine percentage-based traffic control).

---

## Known Limitations / Next Steps

- Restaurant coordinates are hardcoded in the Order→Assignment call (not yet fetched from Catalog Service)
- JWT login is hardcoded to a single test user (no real user registration/BCrypt hashing yet)
- Rate limiting and JWT validation currently live in Order Service, not centralized at the API Gateway (architecturally, the Gateway would be the better location)
- No automated database backup/disaster-recovery strategy
- CDC pipeline (Debezium, MongoDB→Elasticsearch) discussed but not built
- Secrets currently in Kubernetes Secrets (base64-encoded), not a fully-encrypted secret store like AWS Secrets Manager

---

## Notable Bugs Debugged During This Build

- Kafka `__consumer_offsets` replication-factor failure on a single-broker setup
- Spring Boot 4's silent MongoDB property-prefix rename (`spring.data.mongodb` → `spring.mongodb`)
- Docker container-to-container networking vs host port mapping (listeners vs advertised listeners)
- Kubernetes context mismatch (`docker-desktop` vs `minikube`)
- Namespace typo (`agro-rollouts` vs `argo-rollouts`) causing RBAC/ConfigMap failures
- Git Bash path-mangling corrupting `kubectl exec` commands on Windows
- Helm chart files never actually committed to git (`values.yaml`, `Chart.yaml`, most templates)
- OpenTelemetry Spring Boot 4 property path — multiple incorrect attempts before finding the correct structure
- Spring AOP self-invocation breaking `@CircuitBreaker` (required extracting a separate bean)
- `helm uninstall` deleting PersistentVolumeClaims defined inline in chart templates — confirmed via direct testing, not assumption

---

## License

Personal learning project — no license specified.
