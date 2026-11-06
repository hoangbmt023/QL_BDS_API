<div align="center">

# Real Estate Management Platform — Backend API Engine

### Enterprise-grade Spring Boot 4 REST & WebSocket backend powering real estate listings, timeslot-aware viewing appointments, Cloudinary media pipeline, and real-time buyer-seller consultations.

[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Migration%20V1--V8-CC0200?style=flat-square&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Security](https://img.shields.io/badge/Auth-JJWT%200.12.6%20%7C%20BCrypt-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)](docs/API_GUIDE.md)
[![WebSocket](https://img.shields.io/badge/Messaging-STOMP%20WebSocket-010101?style=flat-square&logo=socketdotio&logoColor=white)](docs/SYSTEM_DESIGN.md)
[![Cloudinary](https://img.shields.io/badge/Media-Cloudinary%20CDN-3448C5?style=flat-square&logo=cloudinary&logoColor=white)](https://cloudinary.com/)
[![Docker](https://img.shields.io/badge/Container-Docker%20%7C%20Compose-2496ED?style=flat-square&logo=docker&logoColor=white)](docker-compose.yml)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)

[Quickstart](#quickstart) · [System Highlights](#system-highlights) · [System Architecture](#system-architecture) · [Documentation Hub](#documentation-hub) · [Deployment](#deployment) · [API Playground](#api-playground) · [License](#license)

</div>

---

> [!IMPORTANT]
> **Production Runtime Notice:** The backend service requires Java 21+ and a running PostgreSQL 12+ database (provisioned automatically when using Docker Compose). Sensitive credentials including the JWT secret key (minimum 256 bits), SMTP mail credentials (for OTP emails), and Cloudinary API credentials must be configured in `.env.development` or `.env.production` before launching the application.

---

## System Highlights

| Capability | Module | Technical Description |
| :--- | :--- | :--- |
| **Authentication & Session Security** | `auth_service` | Dual-token authentication with stateless HMAC-SHA256 JWT Access Tokens and persistent Refresh Tokens stored in PostgreSQL. Features in-memory rate-limited OTP email verification via Thymeleaf HTML templates. |
| **User Directory & Role Hierarchy** | `user_service` | 4-tier Role-Based Access Control (`ROLE_USER`, `ROLE_OWNER`, `ROLE_AGENT`, `ROLE_ADMIN`). Supports instantaneous self-upgrade to Owner and formal verification request review workflows for Agent candidates. |
| **Property Listing & Media Pipeline** | `property_service` | Full property catalog management supporting multi-criteria filtering (location, price, area, bedroom/bathroom count), slug generation, similarity algorithms, view counters, and multi-image uploads to Cloudinary CDN. |
| **Viewing Appointment Scheduler** | `viewing_service` | Automated property showing management with conflict-free daily timeslot availability checks, complete status transition engine (`PENDING`, `CONFIRMED`, `REJECTED`, `RESCHEDULED`, `COMPLETED`, `CANCELLED`). |
| **Real-Time Property Inquiries** | `conversation_service` | Property-scoped communication channel connecting prospective buyers with owners or listing agents. Powered by WebSocket STOMP messaging, real-time presence indicators, unread badges, and message edit/recall capabilities. |
| **Wishlist & Saved Properties** | `favorite_service` | Quick-access bookmarking engine enabling users to save properties, check bookmark status in $O(1)$ database index lookups, and perform batch removals. |

---

## System Architecture

The platform operates an integrated Spring Boot architecture combining RESTful endpoints for transactional workflows and a WebSocket STOMP broker for real-time engagement:

```mermaid
graph TD
    Client["Client (Web / Mobile App)"] -->|"REST HTTP Requests"| SecurityFilter["Spring Security Filter Chain (JwtAuthFilter)"]
    Client -->|"WebSocket Handshake (/ws)"| StompBroker["WebSocket STOMP Broker (/topic, /app)"]
    
    SecurityFilter -->|"Validated Bearer Token"| Dispatcher["Spring MVC DispatcherServlet"]
    
    Dispatcher --> AuthCtrl["AuthController (/api/auth)"]
    Dispatcher --> UserCtrl["UserController (/api/users)"]
    Dispatcher --> PropCtrl["PropertyController (/api/properties)"]
    Dispatcher --> ViewCtrl["ViewingController (/api/viewings)"]
    Dispatcher --> FavCtrl["FavoriteController (/api/favorites)"]
    Dispatcher --> ChatCtrl["ConversationController (/api/conversations)"]
    
    StompBroker --> ChatSubsystem["Conversation & Presence Subsystem"]
    
    AuthCtrl --> AuthSvc["AuthService & In-Memory OTP Store"]
    UserCtrl --> UserSvc["UserService & RoleService"]
    PropCtrl --> PropSvc["PropertyService & Similarity Engine"]
    ViewCtrl --> ViewSvc["ViewingService & Timeslot Calculator"]
    FavCtrl --> FavSvc["FavoriteService"]
    ChatCtrl --> ChatSubsystem
    
    AuthSvc -->|"Send OTP Code"| MailSvc["Spring Mail & Thymeleaf Engine"]
    PropSvc -->|"Upload & Transcode Images"| CloudinarySDK["Cloudinary CDN Storage"]
    
    AuthSvc --> DB[(PostgreSQL 15 & Flyway V1-V8)]
    UserSvc --> DB
    PropSvc --> DB
    ViewSvc --> DB
    FavSvc --> DB
    ChatSubsystem --> DB
```

---

## Quickstart

### Prerequisites

- **Java Development Kit (JDK)**: 21 or higher (or run entirely inside Docker)
- **Apache Maven**: 3.8+ (Maven Wrapper `./mvnw.cmd` / `./mvnw` is included)
- **PostgreSQL**: 12+ (or Docker Compose service)
- **Cloudinary Account**: Cloud name, API Key, and Secret (for image uploads)
- **SMTP Mail Provider**: Gmail App Password or standard SMTP relay (for OTP verification)

### 1. Repository Setup

Clone the repository and navigate into the workspace:

```bash
git clone https://github.com/hoangbmt023/qlbds.git
cd qlbds
```

### 2. Environment Configuration

Create `.env.development` in the project root directory:

```properties
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/bds_db
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password

# JWT Security
JWT_SECRET=your_very_long_secret_key_that_should_be_at_least_256_bits_long

# SMTP Mail (Gmail App Password)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Cloudinary Media Storage
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Default Initial Administrator
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
ADMIN_EMAIL=admin@qlbds.com
```

> [!NOTE]
> All `.env.*` files are untracked by Git to protect sensitive credentials.

### 3. Execution

Launch the application using either the included Maven Wrapper or Docker:

```bash
# Option A: Run locally using Maven Wrapper (Windows PowerShell)
./mvnw.cmd spring-boot:run

# Option A: Run locally using Maven Wrapper (Linux / macOS)
./mvnw spring-boot:run

# Option B: Run full container stack (API + PostgreSQL) via Docker Compose
docker-compose up -d
```

The application starts on `http://localhost:8080`.

---

## Documentation Hub

| Document | Scope | Description |
| :--- | :--- | :--- |
| [System Design](docs/SYSTEM_DESIGN.md) | Technical Architecture | Technical breakdown of service modules, real-time WebSocket STOMP design, ERD entity relationships, and state machine lifecycles. |
| [Setup Guide](docs/SETUP_GUIDE.md) | Operations & Deployment | Step-by-step developer environment setup, database Flyway migrations, Cloudinary CDN configuration, and environment dictionary. |
| [API Guide](docs/API_GUIDE.md) | Integration & Contracts | Swagger UI documentation, JWT Bearer flow, response envelopes, error handling, and complete REST endpoint catalog. |
| [Business Flows](docs/BUSINESS_FLOWS.md) | Operational Workflows | End-to-end user journeys (Buyer discovery, appointment negotiation, property publishing, real-time inquiries, role escalation) with sequence diagrams. |
| [Testing Guide](docs/TESTING_GUIDE.md) | Quality Assurance | Test pyramid, JUnit 5 & Mockito test suite breakdown, service coverage matrices, and execution commands. |
| [CI/CD Guide](docs/CICD_GUIDE.md) | Automation & Release | GitHub Actions continuous integration with PostgreSQL test containers and continuous deployment Docker Hub pipelines. |

---

## Deployment

### Option 1: Docker Compose (Recommended for Production)

Deploy both the backend API and PostgreSQL database with persistent storage:

```bash
# Start all services in detached mode
docker-compose up -d

# Follow application logs
docker-compose logs -f app

# Stop the stack
docker-compose down
```

### Option 2: Standalone Production JAR

Package and run an optimized production executable:

```bash
# Package executable JAR skipping unit tests
./mvnw.cmd clean package -DskipTests

# Launch JAR with Production profile
java -Dspring.profiles.active=production -jar target/QLBDS-0.0.1-SNAPSHOT.jar
```

### Option 3: Linux VPS (Systemd Service)

Create a dedicated Systemd unit file at `/etc/systemd/system/qlbds.service`:

```ini
[Unit]
Description=QLBDS Real Estate Management Platform API
After=network.target postgresql.service

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/qlbds
Environment="SPRING_PROFILES_ACTIVE=production"
ExecStart=/usr/bin/java -jar /opt/qlbds/QLBDS-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Reload and activate the unit:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now qlbds
```

---

## API Playground

- **Swagger UI Interactive Explorer**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3.0 Raw JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Postman Collection**: Import [`docs/QL_BDS.postman_collection.json`](docs/QL_BDS.postman_collection.json) directly into Postman for comprehensive end-to-end request suites.

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for full details.
