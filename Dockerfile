FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml and download dependencies (caches this layer to speed up future builds)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the actual source code and build the jar file
COPY src ./src
# Bỏ qua test khi build Docker để tiết kiệm thời gian (vì GitHub Actions đã chạy test rồi)
RUN mvn clean package -DskipTests

# Stage 2: Create the minimal runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Khai báo múi giờ (Tùy chọn cho ứng dụng chạy đúng giờ Việt Nam)
ENV TZ=Asia/Ho_Chi_Minh
RUN apk add --no-cache tzdata

# Copy file jar từ Stage 1 sang Stage 2
COPY --from=builder /app/target/QLBDS-0.0.1-SNAPSHOT.jar app.jar

# Mở cổng 8080 cho ứng dụng
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
