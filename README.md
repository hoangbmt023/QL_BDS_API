# Nền tảng Quản lý Bất Động Sản (Real Estate Management Platform) - Backend API

Backend API cho nền tảng quản lý bất động sản được xây dựng với **Spring Boot 4.0.6** và **Java 21**, cung cấp các dịch vụ toàn diện cho việc quản lý bất động sản, đặt lịch xem nhà, nhắn tin thời gian thực, và quản lý người dùng với các vai trò khác nhau (User, Owner, Agent, Admin).

## 📑 Mục Lục Tài Liệu

Tài liệu chi tiết được sắp xếp trong thư mục `docs/`:

1. [📘 Tài Liệu API](docs/api-document.md) - Hướng dẫn sử dụng các endpoint REST
2. [🗄️ Thiết Kế Cơ Sở Dữ Liệu](docs/database-design.md) - Sơ đồ ERD và mô tả entities
3. [🎯 Luồng Nghiệp Vụ](docs/user-flows.md) - Các kịch bản sử dụng (User Stories)
4. [🧪 Kiểm Thử](docs/testing.md) - Danh sách test cases chính

---

## 🚀 Cài Đặt & Chạy Ứng Dụng

### Yêu Cầu Hệ Thống

- **Java 21+** (hoặc Java 17+)
- **Apache Maven 3.8+** (Maven Wrapper đã được bao gồm)
- **PostgreSQL 12+** (Database chính)
- **Cloudinary Account** (Để upload hình ảnh - tùy chọn)
- **Git** (Để clone repository)

### Cấu Hình Database

Tạo database PostgreSQL cho ứng dụng:

```sql
CREATE DATABASE bds_db;
```

### Cấu Hình Application Properties

Chỉnh sửa file `src/main/resources/application.properties`:

```properties
# Server
server.port=8080
server.servlet.context-path=/

# Database (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/bds_db
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway Migrations
spring.flyway.baselineOnMigrate=true
spring.flyway.locations=classpath:db/migration

# JWT Configuration
jwt.secret=your_very_long_secret_key_that_should_be_at_least_256_bits_long_for_HS256
jwt.expiration=3600000
jwt.refresh-expiration=604800000

# Mail Configuration (for OTP emails)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Cloudinary (Image Upload)
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret

# WebSocket
spring.websocket.enabled=true
```

### Chạy Ứng Dụng

**Phương pháp 1: Sử dụng Maven Wrapper (Khuyên dùng)**

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

**Phương pháp 2: Build và chạy JAR**

```bash
# Build ứng dụng
mvn clean package -DskipTests

# Chạy JAR
java -jar target/QLBDS-0.0.1-SNAPSHOT.jar
```

Ứng dụng sẽ khởi chạy trên `http://localhost:8080`

---

## 🏗️ Kiến Trúc Dự Án

Dự án sử dụng **Domain-Driven Design (DDD)** với các micro-service style packages. Mỗi domain có cấu trúc tương tự:

```
src/main/java/com/example/qlbds/
├── auth_service/              # 🔐 Xác thực & JWT
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── exception/
│
├── user_service/              # 👥 Quản lý người dùng & vai trò
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── mapper/
│
├── property_service/          # 🏠 Quản lý bất động sản
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── mapper/
│   └── specification/
│
├── viewing_service/           # 📅 Quản lý lịch xem nhà
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── conversation_service/      # 💬 Chat & Tin nhắn thời gian thực
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── websocket/
│
├── favorite_service/          # ❤️ Danh sách yêu thích
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── common/                    # 🔧 Exception handlers & utilities
│   ├── exception/
│   ├── response/
│   └── util/
│
├── config/                    # ⚙️ Spring Configuration
│   ├── SecurityConfiguration
│   ├── WebSocketConfig
│   ├── CloudinaryConfig
│   ├── JwtService
│   └── OpenApiConfig
│
├── shared/                    # 📦 Shared code
│   ├── entity/
│   ├── dto/
│   ├── service/
│   └── enums/
│
└── QlbdsApplication.java      # Entry point
```

---

## 🧪 Kiểm Thử

Chạy tất cả các test:

```bash
mvn test
```

Chạy test cho một package cụ thể:

```bash
mvn test -Dtest=com.example.qlbds.property_service.service.*Test
```

Chạy test với code coverage:

```bash
mvn clean test jacoco:report
```

---

## 📚 Công Nghệ & Dependencies

| Công Nghệ | Phiên Bản | Mục Đích |
|-----------|----------|---------|
| Spring Boot | 4.0.6 | Framework chính |
| Spring Security | 4.0.6 | Xác thực & phân quyền |
| Spring Data JPA | 4.0.6 | Truy cập dữ liệu |
| PostgreSQL | 42.x | Database relational |
| Flyway | Latest | Database migrations |
| JWT (JJWT) | 0.12.6 | Token authentication |
| Lombok | Latest | Giảm boilerplate code |
| MapStruct | 1.6.3 | DTO mapping |
| Cloudinary | 1.38.0 | Cloud image storage |
| WebSocket | 4.0.6 | Real-time messaging |
| SpringDoc OpenAPI | 2.8.5 | Swagger/OpenAPI documentation |
| Thymeleaf | 4.0.6 | Email templates |

---

## 🔐 Bảo Mật

- **JWT Authentication**: Token-based stateless authentication
- **Refresh Token**: Long-lived tokens để refresh access tokens
- **Role-Based Access Control (RBAC)**: 4 vai trò (USER, OWNER, AGENT, ADMIN)
- **Password Encryption**: Spring Security PasswordEncoder
- **WebSocket Authentication**: JWT validation trên WebSocket connections
- **CORS**: Cấu hình để cho phép requests từ các origin khác

---

## 📖 Hướng Dẫn Phát Triển

### Thêm Endpoint Mới

1. Tạo controller trong service tương ứng
2. Tạo service layer xử lý business logic
3. Tạo repository interface nếu cần
4. Tạo entity và DTO cho request/response
5. Viết unit tests cho logic mới
6. Update API documentation

### Database Migrations

Tạo migration file mới trong `src/main/resources/db/migration/`:

```sql
-- V{number}__description.sql
-- Ví dụ: V8__add_new_column.sql

ALTER TABLE users ADD COLUMN new_field VARCHAR(255);
```

Flyway sẽ tự động chạy migrations theo thứ tự version.

---

## 🤝 Đóng Góp

1. Fork repository
2. Tạo branch feature (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Mở Pull Request

---

## 📞 Support & Contact

Nếu gặp vấn đề hoặc có câu hỏi, vui lòng tạo issue trên repository.

```bash
mvn test
```

Chi tiết về các kịch bản kiểm thử (Test Cases), vui lòng xem tại [Tài liệu Kiểm thử](docs/testing.md).

---

## 👨‍💻 Thông tin Liên hệ
* **Nhóm/Cá nhân thực hiện:** Huy Hoàng
* **Đề Tài:** Xây dựng Backend API cho Nền tảng Bất động sản và Cho thuê Nhà
