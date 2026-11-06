# Platform Setup & Operations Guide

> Complete, step-by-step developer onboarding manual covering system prerequisites, environment variables dictionary, PostgreSQL Flyway migrations, Cloudinary CDN integration, and runtime deployment modes.

---

## 1. System Prerequisites

Before launching the backend, ensure your environment meets the minimum version criteria:

| Component | Minimum Version | Required For | Notes |
| :--- | :--- | :--- | :--- |
| **Java JDK** | 21 (LTS) | Local compilation & execution | Temurin, Corretto, or Oracle JDK 21. Not needed if running entirely via Docker. |
| **Apache Maven** | 3.8+ | Dependency management | The repo includes Maven Wrapper (`./mvnw.cmd` / `./mvnw`). |
| **PostgreSQL** | 12+ (Recommended 15) | Relational persistence | Local install or automated Docker container. |
| **Docker Engine** | 24+ | Containerization | Required if using Docker Compose. |
| **Cloudinary Account** | Free tier or higher | Photo CDN storage | Required for property image uploads. |
| **SMTP Mail Server** | Gmail or standard SMTP | User email verification | Required for 6-digit OTP delivery. |

---

## 2. Environment Variables Configuration

The platform separates secret credentials from source code by reading from `.env.*` files matching the active Spring profile (`development` or `production`).

### 2.1. Configuration Matrix

Create `.env.development` in the repository root directory (adjacent to `pom.xml`):

| Variable Name | Required | Default / Example | Purpose |
| :--- | :---: | :--- | :--- |
| `DB_URL` | **Yes** | `jdbc:postgresql://localhost:5432/bds_db` | JDBC connection string to the PostgreSQL database. |
| `DB_USERNAME` | **Yes** | `postgres` | Database authentication username. |
| `DB_PASSWORD` | **Yes** | `your_postgres_password` | Database authentication password. |
| `JWT_SECRET` | **Yes** | `d41d8cd98f00b204e9800998ecf8427e...` | Secret key used for signing HMAC-SHA256 tokens (min 256 bits / 32 bytes). |
| `MAIL_USERNAME` | **Yes** | `notifications@example.com` | SMTP account email address. |
| `MAIL_PASSWORD` | **Yes** | `xxxx xxxx xxxx xxxx` | 16-character Gmail App Password or SMTP password. |
| `CLOUDINARY_CLOUD_NAME` | **Yes** | `my-realestate-cloud` | Cloudinary account cloud identifier. |
| `CLOUDINARY_API_KEY` | **Yes** | `123456789012345` | Cloudinary public API key. |
| `CLOUDINARY_API_SECRET`| **Yes** | `abcdefghijklmnopqrstuvwx` | Cloudinary private API secret. |
| `ADMIN_USERNAME` | No | `admin` | Initial Superadmin account username created upon first boot. |
| `ADMIN_PASSWORD` | No | `admin123` | Initial Superadmin account password (hashed upon creation). |
| `ADMIN_EMAIL` | No | `admin@qlbds.com` | Initial Superadmin contact email. |

### 2.2. Sample `.env.development` Template

```properties
# ==========================================
# Database Configuration
# ==========================================
DB_URL=jdbc:postgresql://localhost:5432/bds_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

# ==========================================
# Security Tokens (HMAC-SHA256)
# ==========================================
JWT_SECRET=c3VwZXJfc2VjcmV0X2tleV9mb3JfcWxiZHNfZW50ZXJwcmlzZV9hcGlfMjAyNg==

# ==========================================
# Email SMTP (Gmail App Password)
# ==========================================
MAIL_USERNAME=your_gmail_address@gmail.com
MAIL_PASSWORD=your_16_char_app_password

# ==========================================
# Cloudinary CDN Media Storage
# ==========================================
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# ==========================================
# Default Platform Superadmin
# ==========================================
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
ADMIN_EMAIL=admin@qlbds.com
```

---

## 3. Database Provisioning & Schema Migrations

The application manages all schema definitions using **Flyway Database Migrations**. You do not need to execute manual DDL files.

### 3.1. Local Database Creation

Ensure PostgreSQL is running, then create the target database:

```sql
CREATE DATABASE bds_db;
```

### 3.2. Automated Migration Execution

When Spring Boot boots up, Flyway scans `src/main/resources/db/migration/` and automatically applies pending scripts in order:
- `V1__init.sql`: Core tables, indexes, and constraints.
- `V2__add_slug_to_properties.sql`: Property URL slugs.
- `V3__add_refresh_token.sql`: Long-lived token persistence.
- `V4__add_agent_request.sql`: Agent verification workflow.
- `V5__add_soft_delete.sql`: Soft-delete audit flags (`is_deleted`).
- `V6__update_properties.sql`: View counters & favorite metrics.
- `V7__update_conversations.sql`: Unread counter tracking.
- `V8__update_messages_for_edit_recall.sql`: Message revision & recall support.

---

## 4. Third-Party Integrations Setup

### 4.1. Cloudinary Image Storage

Property listings support multi-image upload directly stored on Cloudinary:
1. Log in to [Cloudinary Console](https://cloudinary.com/console).
2. Copy your **Cloud Name**, **API Key**, and **API Secret** from the dashboard.
3. Paste these values into your `.env.development` file.
4. Uploaded images are automatically stored with optimized formats and CDN delivery URLs.

### 4.2. Gmail SMTP for OTP Delivery

To send 6-digit verification codes to registering users:
1. Navigate to your Google Account > **Security** > **2-Step Verification**.
2. At the bottom, select **App passwords**.
3. Generate a password for "Mail" / "Spring Boot".
4. Copy the resulting 16-character code into `MAIL_PASSWORD` in `.env.development`.

---

## 5. Execution Modes

### Mode 1: Local Development via Maven Wrapper (Recommended)

Run the application locally with active live reload support:

```bash
# Windows PowerShell
./mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Once started, verify access at `http://localhost:8080/swagger-ui.html`.

### Mode 2: Full-Stack Docker Compose

Run the entire system (backend API + PostgreSQL 15 container) with one command without installing Java or PostgreSQL locally:

```bash
# Launch both containers in background
docker-compose up -d

# Inspect live application log output
docker-compose logs -f app

# Stop all containers and network
docker-compose down
```

### Mode 3: Standalone Production JAR

Package an executable artifact:

```bash
# Compile and package JAR skipping test execution
./mvnw.cmd clean package -DskipTests

# Run JAR with the production profile
java -Dspring.profiles.active=production -jar target/QLBDS-0.0.1-SNAPSHOT.jar
```

---

## 6. Troubleshooting & Diagnostics

### Problem: PostgreSQL Connection Refused
- **Symptom**: `org.postgresql.util.PSQLException: Connection to localhost:5432 refused`.
- **Solution**: Verify PostgreSQL is running (`Get-Service postgresql*` on Windows or `sudo systemctl status postgresql` on Linux). Ensure the database `bds_db` exists and credentials in `.env.development` match.

### Problem: Flyway Checksum Mismatch
- **Symptom**: `FlywayException: Validate failed: Migration checksum mismatch for migration version X`.
- **Solution**: Do not alter existing migration scripts that have already been applied to your database. If working in local development and needing a reset, run:
  ```sql
  DROP DATABASE bds_db;
  CREATE DATABASE bds_db;
  ```

### Problem: JWT Secret Key Too Short
- **Symptom**: `WeakKeyException: The specified key byte array is X bits which is not secure enough for HS256`.
- **Solution**: Ensure `JWT_SECRET` contains at least 32 characters (256 bits).

### Problem: Cloudinary Signature Validation Error
- **Symptom**: `com.cloudinary.api.exceptions.GeneralError: Must supply api_key`.
- **Solution**: Check that `CLOUDINARY_API_KEY` and `CLOUDINARY_API_SECRET` are not left as placeholder strings in `.env.development`.

