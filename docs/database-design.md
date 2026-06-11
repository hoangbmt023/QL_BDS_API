# 🗄️ Thiết Kế Cơ Sở Dữ Liệu - Real Estate Management Platform

Tài liệu này mô tả chi tiết kiến trúc, schema, và mối quan hệ giữa các bảng (entities) trong hệ thống.

---

## 🏛️ Sơ Đồ Quan Hệ Thực Thể (ERD)

### 📊 Sơ Đồ Cấp Bậc (Hierarchical View)

**Level 1: Core Entity**
```
users (Tất cả người dùng)
```

**Level 2: User Extensions (1-to-1 upgrade)**
```
users
├── owners (Chủ sở hữu bất động sản)
├── agents (Môi giới bất động sản)
└── (User thường không extend)
```

**Level 3: Main Resources**
```
users
├── properties (Bất động sản - Owner/Agent sở hữu)
├── viewings (Lịch xem nhà - User đặt)
├── favorites (Yêu thích - User bookmark)
├── conversations (Cuộc chat - User tham gia)
├── messages (Tin nhắn - User gửi)
├── refresh_tokens (JWT tokens - User có)
└── agent_requests (Yêu cầu nâng cấp - User gửi)
```

**Level 4: Sub Resources**
```
properties
├── property_images (Hình ảnh của property)
├── viewings (Lịch hẹn xem property này)
├── favorites (Người yêu thích property này)
└── conversations (Chat về property này)

conversations
└── messages (Tin nhắn trong cuộc chat này)
```

### Biểu Đồ Box Diagram

```
┌─────────────────────────────────────────────────────────┐
│                        USERS                            │
│  (email, password, fullName, phone, avatar, role)      │
└─────────────────┬───────────────────────────────────────┘
                  │
        ┌─────────┼──────────┬─────────────┬───────────┐
        │         │          │             │           │
    ┌───▼──┐ ┌───▼──┐ ┌─────▼─────┐ ┌──────▼───┐ ┌────▼────┐
    │OWNERS│ │AGENTS│ │PROPERTIES │ │ VIEWINGS │ │FAVORITES│
    └──────┘ └──────┘ └─────┬─────┘ └──────────┘ └─────────┘
                            │
                    ┌───────┴──────────┐
                    │                  │
              ┌─────▼──────┐    ┌──────▼────────┐
              │PROP_IMAGES │    │CONVERSATIONS  │
              └────────────┘    └──────┬────────┘
                                       │
                                    ┌──▼────────┐
                                    │ MESSAGES  │
                                    └───────────┘

                   ┌──────────────┐
                   │REFRESH_TOKEN │
                   │   (JWT Auth) │
                   └──────────────┘

                   ┌──────────────┐
                   │AGENT_REQUEST │
                   │(Upgrade Flow)│
                   └──────────────┘
```

### Mermaid ERD (Chi Tiết)

```mermaid
erDiagram
    USERS ||--o{ OWNERS : "upgrade_to"
    USERS ||--o{ AGENTS : "upgrade_to"
    USERS ||--o{ REFRESH_TOKENS : "has"
    USERS ||--o{ AGENT_REQUESTS : "sends"
    USERS ||--o{ VIEWINGS : "books"
    USERS ||--o{ FAVORITES : "bookmarks"
    USERS ||--o{ MESSAGES : "sends"
    
    OWNERS ||--o{ PROPERTIES : "owns"
    AGENTS ||--o{ PROPERTIES : "manages"
    
    PROPERTIES ||--o{ PROPERTY_IMAGES : "contains"
    PROPERTIES ||--o{ VIEWINGS : "receives"
    PROPERTIES ||--o{ FAVORITES : "has"
    PROPERTIES ||--o{ CONVERSATIONS : "has"
    
    CONVERSATIONS ||--o{ MESSAGES : "contains"
```

### Bảng Relationships

| Source | Target | Type | Cardinality | Foreign Key | Mô Tả |
|--------|--------|------|------------|-------------|-------|
| users | owners | 1-to-1 | 1:1 | owners.user_id | Nâng cấp thành chủ nhà |
| users | agents | 1-to-1 | 1:1 | agents.user_id | Nâng cấp thành môi giới |
| owners | properties | 1-to-many | 1:N | properties.owner_id | Chủ nhà sở hữu BDS |
| agents | properties | 1-to-many | 1:N | properties.agent_id | Môi giới quản lý BDS |
| properties | property_images | 1-to-many | 1:N | property_images.property_id | BDS có nhiều ảnh |
| properties | viewings | 1-to-many | 1:N | viewings.property_id | BDS có nhiều lịch hẹn |
| properties | favorites | 1-to-many | 1:N | favorites.property_id | BDS được yêu thích |
| properties | conversations | 1-to-many | 1:N | conversations.property_id | BDS có nhiều cuộc chat |
| users | viewings | 1-to-many | 1:N | viewings.user_id | User đặt nhiều lịch |
| users | favorites | 1-to-many | 1:N | favorites.user_id | User yêu thích nhiều BDS |
| users | messages | 1-to-many | 1:N | messages.sender_id | User gửi nhiều tin |
| users | refresh_tokens | 1-to-many | 1:N | refresh_tokens.user_id | User có nhiều tokens |
| users | agent_requests | 1-to-many | 1:N | agent_requests.user_id | User gửi yêu cầu |
| conversations | messages | 1-to-many | 1:N | messages.conversation_id | Chat có nhiều tin nhắn |

---

## 📋 Chi Tiết Các Bảng (Tables)

### 1. **users** - Bảng Người Dùng

Lưu trữ thông tin cơ bản về tất cả người dùng trong hệ thống.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|---------|-------------|---------|-------|
| id | BIGINT | PRIMARY KEY | ID người dùng |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Email duy nhất |
| password | VARCHAR(255) | NOT NULL | Password đã hash (bcrypt) |
| full_name | VARCHAR(255) | NOT NULL | Tên đầy đủ |
| phone | VARCHAR(20) | - | Số điện thoại |
| avatar | VARCHAR(500) | - | URL ảnh đại diện |
| role | ENUM | NOT NULL, DEFAULT='USER' | Vai trò: USER, OWNER, AGENT, ADMIN |
| is_active | BOOLEAN | DEFAULT=true | Trạng thái kích hoạt |
| is_deleted | BOOLEAN | DEFAULT=false | Soft delete flag |
| created_at | TIMESTAMP | NOT NULL | Thời gian tạo |
| updated_at | TIMESTAMP | - | Thời gian cập nhật |

**Ví dụ:**
```sql
INSERT INTO users (email, password, full_name, phone, role, created_at) 
VALUES ('user@example.com', '$2a$10$...', 'Nguyễn Văn A', '0912345678', 'USER', NOW());
```

---

### 2. **owners** - Bảng Chủ Nhà (Optional extended profile)

Lưu trữ thông tin bổ sung cho người dùng có vai trò OWNER.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|---------|-------------|---------|-------|
| id | BIGINT | PRIMARY KEY | ID chủ nhà |
| user_id | BIGINT | FOREIGN KEY, UNIQUE | Liên kết đến users |
| company_name | VARCHAR(255) | - | Tên công ty (nếu có) |
| business_license | VARCHAR(255) | - | Số giấy phép kinh doanh |
| verification_status | ENUM | DEFAULT='PENDING' | PENDING, VERIFIED, REJECTED |
| verified_at | TIMESTAMP | - | Thời gian xác minh |
| created_at | TIMESTAMP | NOT NULL | Thời gian tạo |

---

### 3. **agents** - Bảng Môi Giới

Lưu trữ thông tin bổ sung cho người dùng có vai trò AGENT.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|---------|-------------|---------|-------|
| id | BIGINT | PRIMARY KEY | ID môi giới |
| user_id | BIGINT | FOREIGN KEY, UNIQUE | Liên kết đến users |
| license_number | VARCHAR(255) | UNIQUE | Số giấy phép môi giới |
| commission_rate | DECIMAL(5,2) | - | Tỷ lệ hoa hồng (%) |
| total_sales | DECIMAL(15,2) | DEFAULT=0 | Tổng doanh số |
| is_verified | BOOLEAN | DEFAULT=false | Đã xác minh |
| verified_at | TIMESTAMP | - | Thời gian xác minh |
| created_at | TIMESTAMP | NOT NULL | Thời gian tạo |

---

### 4. **agent_requests** - Bảng Yêu Cầu Nâng Cấp Agent

Quản lý các yêu cầu từ người dùng muốn nâng cấp lên Agent.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|---------|-------------|---------|-------|
| id | BIGINT | PRIMARY KEY | ID yêu cầu |
| user_id | BIGINT | FOREIGN KEY, NOT NULL | Người gửi yêu cầu |
| content | TEXT | NOT NULL | Nội dung yêu cầu |
| status | ENUM | DEFAULT='PENDING' | PENDING, APPROVED, REJECTED |
| rejection_reason | TEXT | - | Lý do từ chối (nếu có) |
| created_at | TIMESTAMP | NOT NULL | Thời gian gửi |
| reviewed_at | TIMESTAMP | - | Thời gian xét duyệt |
| reviewed_by | BIGINT | FOREIGN KEY | Admin xét duyệt |

---

### 5. **refresh_tokens** - Bảng Refresh Token

Lưu trữ refresh tokens để cấp lại access tokens khi hết hạn.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|---------|-------------|---------|-------|
| id | BIGINT | PRIMARY KEY | ID token |
| user_id | BIGINT | FOREIGN KEY, NOT NULL | Người dùng |
| token | VARCHAR(500) | UNIQUE, NOT NULL | Refresh token |
| expiry_date | TIMESTAMP | NOT NULL | Ngày hết hạn |
| revoked | BOOLEAN | DEFAULT=false | Đã bị thu hồi |
| created_at | TIMESTAMP | NOT NULL | Thời gian tạo |

---

### 6. **properties** - Bảng Bất Động Sản

Lưu trữ thông tin chi tiết về từng bất động sản được đăng.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|---------|-------------|---------|-------|
| id | BIGINT | PRIMARY KEY | ID bất động sản |
| owner_id | BIGINT | FOREIGN KEY, NOT NULL | Chủ sở hữu |
| agent_id | BIGINT | FOREIGN KEY | Môi giới (nếu có) |
| title | VARCHAR(255) | NOT NULL | Tiêu đề |
| slug | VARCHAR(255) | UNIQUE, NOT NULL | URL slug |
| description | TEXT | - | Mô tả chi tiết |
| price | DECIMAL(15,2) | NOT NULL | Giá bán/cho thuê |
| area | DECIMAL(10,2) | NOT NULL | Diện tích (m²) |
| bedrooms | INT | - | Số phòng ngủ |
| bathrooms | INT | - | Số phòng tắm |
| address | VARCHAR(255) | NOT NULL | Địa chỉ |
| city | VARCHAR(100) | NOT NULL | Thành phố |
| district | VARCHAR(100) | NOT NULL | Quận/Huyện |
| latitude | DECIMAL(10,8) | - | Vĩ độ (map) |
| longitude | DECIMAL(11,8) | - | Kinh độ (map) |
| property_type | ENUM | - | APARTMENT, HOUSE, VILLA, LAND, OFFICE |
| status | ENUM | DEFAULT='PENDING' | PENDING, APPROVED, REJECTED, SOLD, DELETED |
| visibility | BOOLEAN | DEFAULT=false | Hiển thị công khai |
| view_count | INT | DEFAULT=0 | Số lượt xem |
| rejection_reason | TEXT | - | Lý do từ chối (Admin) |
| is_deleted | BOOLEAN | DEFAULT=false | Soft delete flag |
| created_at | TIMESTAMP | NOT NULL | Thời gian tạo |
| updated_at | TIMESTAMP | - | Thời gian cập nhật |

**Ví dụ:**
```sql
INSERT INTO properties (owner_id, title, slug, price, area, bedrooms, address, city, district, status, created_at)
VALUES (1, 'Nhà phố 3 tầng', 'nha-pho-3-tang', 5000000000, 100.5, 3, '123 Nguyễn Huệ', 'HCM', 'Q1', 'PENDING', NOW());
```

---

### 7. **property_images** - Bảng Hình Ảnh Bất Động Sản

Lưu trữ đường dẫn hình ảnh của từng bất động sản.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|---------|-------------|---------|-------|
| id | BIGINT | PRIMARY KEY | ID hình ảnh |
| property_id | BIGINT | FOREIGN KEY, NOT NULL | Bất động sản |
| image_url | VARCHAR(500) | NOT NULL | URL hình ảnh (Cloudinary) |
| is_primary | BOOLEAN | DEFAULT=false | Hình chính |
| display_order | INT | DEFAULT=0 | Thứ tự hiển thị |
| created_at | TIMESTAMP | NOT NULL | Thời gian tải lên |
| is_deleted | BOOLEAN | DEFAULT=false | Soft delete flag |

---

### 8. **viewings** - Bảng Lịch Xem Nhà

Quản lý các lịch hẹn xem bất động sản.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|---------|-------------|---------|-------|
| id | BIGINT | PRIMARY KEY | ID lịch hẹn |
| property_id | BIGINT | FOREIGN KEY, NOT NULL | Bất động sản |
| user_id | BIGINT | FOREIGN KEY, NOT NULL | Người đặt lịch |
| viewing_date | TIMESTAMP | NOT NULL | Ngày/giờ xem |
| note | TEXT | - | Ghi chú |
| status | ENUM | DEFAULT='PENDING' | PENDING, CONFIRMED, CANCELLED, COMPLETED |
| rejection_reason | TEXT | - | Lý do từ chối |
| is_deleted | BOOLEAN | DEFAULT=false | Soft delete flag |
| created_at | TIMESTAMP | NOT NULL | Thời gian tạo |
| updated_at | TIMESTAMP | - | Thời gian cập nhật |

**Indices:**
```sql
CREATE INDEX idx_property_viewing ON viewings(property_id);
CREATE INDEX idx_user_viewing ON viewings(user_id);
CREATE INDEX idx_viewing_status ON viewings(status);
```

---

### 9. **favorites** - Bảng Yêu Thích

Lưu trữ các bất động sản mà người dùng yêu thích.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|---------|-------------|---------|-------|
| id | BIGINT | PRIMARY KEY | ID favorite |
| user_id | BIGINT | FOREIGN KEY, NOT NULL | Người dùng |
| property_id | BIGINT | FOREIGN KEY, NOT NULL | Bất động sản |
| created_at | TIMESTAMP | NOT NULL | Thời gian thêm |
| is_deleted | BOOLEAN | DEFAULT=false | Soft delete flag |

**Constraints:**
```sql
UNIQUE(user_id, property_id)
```

---

### 10. **conversations** - Bảng Cuộc Hội Thoại

Lưu trữ các cuộc trò chuyện giữa người dùng.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|---------|-------------|---------|-------|
| id | BIGINT | PRIMARY KEY | ID hội thoại |
| property_id | BIGINT | FOREIGN KEY, NOT NULL | Bất động sản liên quan |
| user1_id | BIGINT | FOREIGN KEY, NOT NULL | Người tham gia 1 |
| user2_id | BIGINT | FOREIGN KEY, NOT NULL | Người tham gia 2 |
| last_message_at | TIMESTAMP | - | Lần tin nhắn gần nhất |
| is_active | BOOLEAN | DEFAULT=true | Cuộc hội thoại còn hoạt động |
| created_at | TIMESTAMP | NOT NULL | Thời gian tạo |
| updated_at | TIMESTAMP | - | Thời gian cập nhật |

**Constraints:**
```sql
UNIQUE(property_id, user1_id, user2_id)
CHECK (user1_id < user2_id)
```

---

### 11. **messages** - Bảng Tin Nhắn

Lưu trữ từng tin nhắn trong các cuộc hội thoại.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|---------|-------------|---------|-------|
| id | BIGINT | PRIMARY KEY | ID tin nhắn |
| conversation_id | BIGINT | FOREIGN KEY, NOT NULL | Cuộc hội thoại |
| sender_id | BIGINT | FOREIGN KEY, NOT NULL | Người gửi |
| content | TEXT | NOT NULL | Nội dung |
| is_read | BOOLEAN | DEFAULT=false | Đã đọc |
| read_at | TIMESTAMP | - | Thời gian đọc |
| created_at | TIMESTAMP | NOT NULL | Thời gian gửi |

**Indices:**
```sql
CREATE INDEX idx_conversation_messages ON messages(conversation_id);
CREATE INDEX idx_sender_messages ON messages(sender_id);
CREATE INDEX idx_message_timestamp ON messages(created_at DESC);
```

---

### 12. **otps** - Bảng OTP

Lưu trữ mã OTP cho xác minh email.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Mô Tả |
|---------|-------------|---------|-------|
| id | BIGINT | PRIMARY KEY | ID OTP |
| email | VARCHAR(255) | NOT NULL | Email nhận OTP |
| otp_code | VARCHAR(6) | NOT NULL | Mã OTP (6 chữ số) |
| expiry_time | TIMESTAMP | NOT NULL | Thời gian hết hạn |
| is_verified | BOOLEAN | DEFAULT=false | Đã xác minh |
| attempts | INT | DEFAULT=0 | Số lần nhập sai |
| created_at | TIMESTAMP | NOT NULL | Thời gian tạo |

**Ràng buộc:**
- OTP hết hạn sau 15 phút
- Tối đa 5 lần nhập sai mã OTP

---

## 🔑 Keys & Relationships Details

Tất cả các bảng sử dụng `BIGINT` cho primary keys được tự động tăng (auto-increment).

### Primary Keys (PK):
```
users.id, owners.id, agents.id, properties.id, property_images.id,
viewings.id, favorites.id, conversations.id, messages.id,
refresh_tokens.id, agent_requests.id, otps.id
```

### Foreign Keys (FK) - Core Relationships:

#### 👥 User Hierarchy
```
owners.user_id → users.id (ONE-to-ONE)
agents.user_id → users.id (ONE-to-ONE)
```

#### 🏠 Property Ownership
```
properties.owner_id → users.id (MANY-to-ONE)
properties.agent_id → agents.id (MANY-to-ONE, optional)
```

#### 📸 Property Media
```
property_images.property_id → properties.id (MANY-to-ONE)
```

#### 📅 Appointment Management
```
viewings.property_id → properties.id (MANY-to-ONE)
viewings.user_id → users.id (MANY-to-ONE)
```

#### ❤️ Favorites/Bookmarks
```
favorites.user_id → users.id (MANY-to-ONE)
favorites.property_id → properties.id (MANY-to-ONE)
UNIQUE(user_id, property_id)
```

#### 💬 Messaging System
```
conversations.property_id → properties.id (MANY-to-ONE)
conversations.user1_id → users.id (MANY-to-ONE)
conversations.user2_id → users.id (MANY-to-ONE)
UNIQUE(property_id, user1_id, user2_id)
messages.conversation_id → conversations.id (MANY-to-ONE)
messages.sender_id → users.id (MANY-to-ONE)
```

#### 🔐 Authentication
```
refresh_tokens.user_id → users.id (MANY-to-ONE)
```

#### 🎓 Role Management
```
agent_requests.user_id → users.id (MANY-to-ONE)
agent_requests.reviewed_by → users.id (MANY-to-ONE, optional)
```

#### 🔑 OTP Verification
```
otps.email (NOT FOREIGN KEY - email text, no PK ref)
```

---

## 📊 Indexes Để Cải Thiện Hiệu Năng

```sql
-- Users
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_user_role ON users(role);

-- Properties
CREATE INDEX idx_property_status ON properties(status);
CREATE INDEX idx_property_owner ON properties(owner_id);
CREATE INDEX idx_property_agent ON properties(agent_id);
CREATE INDEX idx_property_city_district ON properties(city, district);
CREATE INDEX idx_property_visibility ON properties(visibility);

-- Viewings
CREATE INDEX idx_viewing_property ON viewings(property_id);
CREATE INDEX idx_viewing_user ON viewings(user_id);
CREATE INDEX idx_viewing_status ON viewings(status);
CREATE INDEX idx_viewing_date ON viewings(viewing_date);

-- Favorites
CREATE INDEX idx_favorite_user ON favorites(user_id);
CREATE INDEX idx_favorite_property ON favorites(property_id);

-- Messages
CREATE INDEX idx_message_conversation ON messages(conversation_id);
CREATE INDEX idx_message_timestamp ON messages(created_at);
```

---

## 🔒 Data Validation & Constraints

### User Validation:
- Email: Định dạng email hợp lệ, duy nhất
- Password: Tối thiểu 8 ký tự, phải chứa hoa, thường, số, ký tự đặc biệt
- Phone: Định dạng số điện thoại Việt Nam (10-11 chữ số)

### Property Validation:
- Price: Phải > 0
- Area: Phải > 0
- Title: Tối thiểu 10 ký tự, tối đa 255 ký tự
- Viewing Date: Phải là ngày trong tương lai

### Soft Delete:
- Thay vì xóa dữ liệu, đặt flag `is_deleted = true`
- Tất cả queries đều bỏ qua các bản ghi bị xóa (WHERE is_deleted = false)

---

## 📈 Database Growth Estimates

Giả sử hệ thống có:
- 100,000 users
- 50,000 properties
- 500,000 messages

Kích thước database ước tính: **500 MB - 1 GB** (dựa trên dữ liệu và indices)
    
    USER ||--o{ VIEWING : "books"
    PROPERTY ||--o{ VIEWING : "is scheduled for"
    
    USER ||--o{ FAVORITE : "likes"
    PROPERTY ||--o{ FAVORITE : "is liked in"
    
    PROPERTY ||--o{ CONVERSATION : "about"
    USER ||--o{ CONVERSATION : "participates (user1/user2)"
    CONVERSATION ||--o{ MESSAGE : "contains"
    USER ||--o{ MESSAGE : "sends"
```

---

## 2. Cấu trúc các Table Chính

### 2.1. Nhóm Auth & User
* **`users`**: Bảng trung tâm lưu trữ mọi loại tài khoản. Phân biệt quyền qua cột `role`.
* **`refresh_tokens`**: Dùng cho cơ chế xoay vòng JWT (Rotation), bảo mật API đăng nhập.
* **`agent_requests`**: Nơi lưu trữ các yêu cầu nộp đơn để trở thành Môi giới (Agent), chờ Admin duyệt.

### 2.2. Nhóm Bất động sản (Properties)
* **`properties`**: Lưu thông tin chi tiết nhà/đất (giá, diện tích, địa chỉ, phân loại, trạng thái).
* **`property_images`**: Cho phép 1 tài sản có nhiều hình ảnh.
* **`favorites`**: Lưu trạng thái "Lưu tin" của người dùng đối với các bất động sản họ quan tâm.

### 2.3. Nhóm Giao dịch & Tương tác
* **`viewings`**: Quản lý lịch hẹn xem nhà. Người dùng gửi yêu cầu, Chủ nhà/Môi giới sẽ cập nhật trạng thái (Chờ duyệt -> Chấp nhận/Từ chối).
* **`conversations`**: Chứa thông tin các cuộc hội thoại giữa 2 người dùng (User - Owner/Agent) về 1 bất động sản cụ thể.
* **`messages`**: Lưu trữ chi tiết nội dung từng tin nhắn trong hội thoại.
