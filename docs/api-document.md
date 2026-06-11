# 📘 Tài Liệu API - Real Estate Management Platform

Tài liệu này mô tả chi tiết tất cả các endpoint REST API của hệ thống. API được tài liệu hóa bằng **OpenAPI 3.0** (Swagger UI) và có thể được kiểm thử trực tiếp.

---

## 🌐 Truy Cập API Documentation

### Swagger UI (Interactive)

Sau khi ứng dụng khởi chạy, truy cập:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **OpenAPI YAML**: [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)

### Cách Sử Dụng JWT Authentication trong Swagger

1. Đăng nhập để lấy token:
   - Gọi endpoint `POST /api/auth/login` với email và password
   - Copy `accessToken` từ response

2. Cấu hình Authorization:
   - Click nút **Authorize** (🔒) ở góc phải trên cùng
   - Nhập: `Bearer <your_access_token>` (ví dụ: `Bearer eyJhbGc...`)
   - Click **Authorize** rồi **Close**

3. Sau đó tất cả các request sẽ tự động gắn kèm token vào header `Authorization`

---

## 📋 Danh Sách Các API Chính

### 🔐 **Authentication API** → `/api/auth`

#### 1. Đăng ký tài khoản (Register)

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "fullName": "Nguyễn Văn A",
  "phone": "0912345678"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Đăng ký thành công. Vui lòng kiểm tra email để xác thực OTP.",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyễn Văn A",
    "role": "USER"
  }
}
```

#### 2. Xác thực OTP (Verify OTP)

```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Xác thực email thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 3600
  }
}
```

#### 3. Đăng nhập (Login)

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "fullName": "Nguyễn Văn A",
      "role": "USER"
    }
  }
}
```

#### 4. Làm mới Token (Refresh Token)

```http
POST /api/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 3600
  }
}
```

#### 5. Gửi lại OTP (Resend OTP)

```http
POST /api/auth/resend-otp
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "OTP đã được gửi lại. Vui lòng kiểm tra email."
}
```

#### 6. Đăng xuất (Logout)

```http
POST /api/auth/logout
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Đăng xuất thành công"
}
```

---

### 👥 **User API** → `/api/users`

#### 1. Lấy Thông Tin User Hiện Tại

```http
GET /api/users/me
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyễn Văn A",
    "phone": "0912345678",
    "avatar": "https://...",
    "role": "USER",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

#### 2. Cập Nhật Hồ Sơ Người Dùng

```http
PUT /api/users/profile
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "fullName": "Nguyễn Văn A Updated",
  "phone": "0987654321",
  "avatar": "https://..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Cập nhật hồ sơ thành công",
  "data": { /* updated user info */ }
}
```

#### 3. Tìm Kiếm Người Dùng

```http
GET /api/users/search?keyword=nguyễn&limit=10&offset=0
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "fullName": "Nguyễn Văn A",
      "email": "user@example.com",
      "avatar": "https://..."
    }
  ],
  "pagination": {
    "total": 1,
    "limit": 10,
    "offset": 0
  }
}
```

#### 4. Xóa Tài Khoản

```http
DELETE /api/users/{userId}
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Tài khoản đã được xóa"
}
```

---

### 🎓 **Role & Agent Request API** → `/api/roles`

#### 1. Nâng Cấp lên Owner

```http
POST /api/roles/upgrade-to-owner
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Nâng cấp thành Owner thành công"
}
```

#### 2. Gửi Yêu Cầu Trở Thành Agent

```http
POST /api/roles/upgrade-to-agent
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "content": "Tôi muốn trở thành agent bất động sản..."
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Yêu cầu đã được gửi. Chờ xét duyệt từ Admin.",
  "data": {
    "id": 1,
    "userId": 1,
    "status": "PENDING",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

#### 3. Lấy Danh Sách Yêu Cầu Nâng Cấp (Admin Only)

```http
GET /api/roles/agent-requests?status=PENDING&limit=20&offset=0
Authorization: Bearer <admin_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 2,
      "user": { "fullName": "User Name", "email": "..." },
      "content": "...",
      "status": "PENDING",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "pagination": { "total": 5, "limit": 20, "offset": 0 }
}
```

#### 4. Phê Duyệt Yêu Cầu Agent (Admin Only)

```http
PATCH /api/roles/agent-requests/{requestId}/approve
Authorization: Bearer <admin_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Yêu cầu đã được phê duyệt. User đã được nâng cấp lên Agent."
}
```

#### 5. Từ Chối Yêu Cầu Agent (Admin Only)

```http
PATCH /api/roles/agent-requests/{requestId}/reject
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "reason": "Hồ sơ chưa đủ điều kiện"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Yêu cầu đã bị từ chối"
}
```

---

### 🏠 **Property API** → `/api/properties`

#### 1. Tạo Bài Đăng Bất Động Sản

```http
POST /api/properties
Authorization: Bearer <owner_or_agent_token>
Content-Type: application/json

{
  "title": "Nhà phố 3 tầng đẹp ở Quận 1",
  "description": "Chi tiết mô tả bất động sản...",
  "price": 5000000000,
  "area": 100.5,
  "bedrooms": 3,
  "bathrooms": 2,
  "address": "123 Đường Nguyễn Huệ",
  "city": "HCM",
  "district": "Q1",
  "agentId": null
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Nhà phố 3 tầng đẹp ở Quận 1",
    "slug": "nha-pho-3-tang-dep-o-quan-1",
    "price": 5000000000,
    "status": "PENDING",
    "visibility": false,
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

#### 2. Lấy Danh Sách Bất Động Sản (Có Filter & Pagination)

```http
GET /api/properties?page=0&size=20&minPrice=1000000000&maxPrice=10000000000&district=Q1&bedrooms=2
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Nhà phố 3 tầng",
      "slug": "nha-pho-3-tang",
      "price": 5000000000,
      "area": 100.5,
      "bedrooms": 3,
      "bathrooms": 2,
      "district": "Q1",
      "city": "HCM",
      "status": "APPROVED",
      "visibility": true,
      "viewCount": 45,
      "images": ["https://...", "https://..."]
    }
  ],
  "pagination": {
    "totalElements": 150,
    "totalPages": 8,
    "currentPage": 0,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

#### 3. Lấy Chi Tiết Bất Động Sản (By Slug)

```http
GET /api/properties/{slug}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Nhà phố 3 tầng",
    "slug": "nha-pho-3-tang",
    "description": "Chi tiết...",
    "price": 5000000000,
    "area": 100.5,
    "bedrooms": 3,
    "bathrooms": 2,
    "address": "123 Đường Nguyễn Huệ",
    "district": "Q1",
    "city": "HCM",
    "status": "APPROVED",
    "visibility": true,
    "viewCount": 45,
    "owner": {
      "id": 1,
      "fullName": "Chủ nhà",
      "phone": "0912345678",
      "avatar": "https://..."
    },
    "images": [
      {
        "id": 1,
        "imageUrl": "https://...",
        "isPrimary": true
      }
    ],
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

#### 4. Cập Nhật Bất Động Sản

```http
PUT /api/properties/{propertyId}
Authorization: Bearer <owner_token>
Content-Type: application/json

{
  "title": "Nhà phố 3 tầng mới",
  "description": "Chi tiết cập nhật...",
  "price": 5500000000,
  "area": 105.0,
  "bedrooms": 3,
  "bathrooms": 2,
  "address": "456 Đường Nguyễn Huệ",
  "city": "HCM",
  "district": "Q1"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Cập nhật bất động sản thành công",
  "data": { /* updated property */ }
}
```

#### 5. Xóa Bất Động Sản (Soft Delete)

```http
DELETE /api/properties/{propertyId}
Authorization: Bearer <owner_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Bất động sản đã bị xóa"
}
```

#### 6. Upload Hình Ảnh Bất Động Sản

```http
POST /api/properties/{propertyId}/images
Authorization: Bearer <owner_token>
Content-Type: multipart/form-data

files: [file1.jpg, file2.jpg, ...]
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Tải lên hình ảnh thành công",
  "data": [
    {
      "id": 1,
      "imageUrl": "https://cloudinary.com/...",
      "isPrimary": false
    }
  ]
}
```

#### 7. Cập Nhật Hình Ảnh

```http
PATCH /api/properties/{propertyId}/images/{imageId}
Authorization: Bearer <owner_token>
Content-Type: multipart/form-data

file: new_image.jpg
```

#### 8. Xóa Hình Ảnh

```http
DELETE /api/properties/{propertyId}/images/{imageId}
Authorization: Bearer <owner_token>
```

#### 9. Đổi Trạng Thái Bất Động Sản (Admin Only)

```http
PATCH /api/properties/{propertyId}/status
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "status": "APPROVED",
  "rejectionReason": null
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Trạng thái bất động sản đã được cập nhật"
}
```

#### 10. Lấy Thống Kê Bất Động Sản

```http
GET /api/properties/{propertyId}/analytics
Authorization: Bearer <owner_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "propertyId": 1,
    "viewCount": 150,
    "viewingAppointments": 12,
    "totalConversations": 8,
    "conversionRate": 8.0
  }
}
```

---

### 📅 **Viewing (Lịch Xem Nhà) API** → `/api/viewings`

#### 1. Tạo Lịch Xem Nhà

```http
POST /api/viewings
Authorization: Bearer <user_token>
Content-Type: application/json

{
  "propertyId": 1,
  "viewingDate": "2024-02-15T14:30:00",
  "note": "Muốn xem vào chiều"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "propertyId": 1,
    "viewingDate": "2024-02-15T14:30:00Z",
    "status": "PENDING",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

#### 2. Lấy Lịch Xem Của User

```http
GET /api/viewings/me?page=0&size=10&status=PENDING
Authorization: Bearer <user_token>
```

#### 3. Lấy Chi Tiết Lịch Xem

```http
GET /api/viewings/{viewingId}
Authorization: Bearer <access_token>
```

#### 4. Cập Nhật Trạng Thái Lịch Xem (Owner Only)

```http
PATCH /api/viewings/{viewingId}/status
Authorization: Bearer <owner_token>
Content-Type: application/json

{
  "status": "CONFIRMED"
}
```

**Các trạng thái hợp lệ:** `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`

#### 5. Dời Lịch Hẹn (Reschedule)

```http
PATCH /api/viewings/{viewingId}/reschedule
Authorization: Bearer <user_token>
Content-Type: application/json

{
  "newViewingDate": "2024-02-20T15:00:00"
}
```

---

### 💬 **Conversation & Message API** → `/api/conversations`

#### 1. Tạo/Lấy Cuộc Hội Thoại

```http
POST /api/conversations
Authorization: Bearer <user_token>
Content-Type: application/json

{
  "propertyId": 1,
  "recipientId": 2
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "propertyId": 1,
    "participants": [
      { "id": 1, "fullName": "User A" },
      { "id": 2, "fullName": "User B" }
    ],
    "lastMessageAt": "2024-01-15T10:30:00Z"
  }
}
```

#### 2. Lấy Danh Sách Cuộc Hội Thoại

```http
GET /api/conversations?page=0&size=20
Authorization: Bearer <user_token>
```

#### 3. Lấy Danh Sách Tin Nhắn

```http
GET /api/conversations/{conversationId}/messages?page=0&size=50
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "content": "Xin chào, bạn quan tâm nhà này không?",
      "sender": {
        "id": 1,
        "fullName": "Chủ nhà"
      },
      "isRead": true,
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "pagination": { "totalElements": 100, "currentPage": 0, "pageSize": 50 }
}
```

#### 4. Gửi Tin Nhắn

```http
POST /api/conversations/{conversationId}/messages
Authorization: Bearer <user_token>
Content-Type: application/json

{
  "content": "Vâng, tôi rất quan tâm!"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "content": "Vâng, tôi rất quan tâm!",
    "sender": { "id": 1, "fullName": "User A" },
    "isRead": false,
    "createdAt": "2024-01-15T10:35:00Z"
  }
}
```

#### 5. Kiểm Tra Trạng Thái Online (Presence)

```http
GET /api/presence/{userId}
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "isOnline": true,
    "lastSeenAt": "2024-01-15T10:35:00Z"
  }
}
```

---

### ❤️ **Favorite API** → `/api/favorites`

#### 1. Thêm/Xóa Bất Động Sản Yêu Thích

```http
POST /api/favorites/{propertyId}
Authorization: Bearer <user_token>
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Đã thêm vào danh sách yêu thích"
}
```

hoặc (200 OK nếu xóa):
```json
{
  "success": true,
  "message": "Đã xóa khỏi danh sách yêu thích"
}
```

#### 2. Lấy Danh Sách Yêu Thích

```http
GET /api/favorites?page=0&size=20
Authorization: Bearer <user_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "property": {
        "id": 1,
        "title": "Nhà phố 3 tầng",
        "price": 5000000000,
        "area": 100.5,
        "image": "https://..."
      },
      "addedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "pagination": { "totalElements": 10, "currentPage": 0, "pageSize": 20 }
}
```

#### 3. Xóa Nhiều Yêu Thích (Bulk Delete)

```http
POST /api/favorites/bulk-delete
Authorization: Bearer <user_token>
Content-Type: application/json

{
  "propertyIds": [1, 2, 3]
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Đã xóa 3 bất động sản khỏi danh sách yêu thích"
}
```

---

## 🔄 WebSocket Real-time Features

### Kết Nối WebSocket

```javascript
// JavaScript Client
const token = localStorage.getItem('accessToken');
const socket = new WebSocket(
  `ws://localhost:8080/ws?token=${token}`
);

socket.onopen = () => console.log('Connected');
socket.onmessage = (event) => {
  const message = JSON.parse(event.data);
  console.log('Message:', message);
};
```

### Các Events WebSocket

**1. Khi có tin nhắn mới (Message Event)**
```json
{
  "type": "NEW_MESSAGE",
  "data": {
    "conversationId": 1,
    "message": { "id": 1, "content": "..." }
  }
}
```

**2. Cập nhật trạng thái online (Presence Event)**
```json
{
  "type": "USER_PRESENCE",
  "data": {
    "userId": 1,
    "isOnline": true,
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

---

## 🚨 Error Responses

Tất cả các lỗi sẽ trả về theo định dạng:

```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Bất động sản không tồn tại",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

### HTTP Status Codes

| Code | Ý Nghĩa |
|------|---------|
| 200 | OK - Request thành công |
| 201 | Created - Resource được tạo |
| 400 | Bad Request - Request không hợp lệ |
| 401 | Unauthorized - Chưa đăng nhập |
| 403 | Forbidden - Không có quyền truy cập |
| 404 | Not Found - Resource không tồn tại |
| 409 | Conflict - Dữ liệu bị xung đột |
| 500 | Internal Server Error - Lỗi server |

---

## 📦 Rate Limiting

- **OTP requests**: Tối đa 5 requests / 1 giờ cho 1 email
- **Login attempts**: Tối đa 10 lần đăng nhập thất bại / 15 phút

---

## 💡 Tips

- **Token Expiration**: Access token hết hạn sau 1 giờ, sử dụng refresh token để lấy cặp token mới
- **Pagination**: Tất cả các endpoint list đều hỗ trợ `page` và `size` parameters
- **Filtering**: Sử dụng query parameters để filter (ví dụ: `?minPrice=1000000&maxPrice=10000000`)
- **Sorting**: Hỗ trợ sắp xếp theo `sort=field,desc` hoặc `sort=field,asc`
