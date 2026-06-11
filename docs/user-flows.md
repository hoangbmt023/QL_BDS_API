# 🎯 Luồng Nghiệp Vụ (User Flows & Use Cases)

Tài liệu này mô tả chi tiết các kịch bản sử dụng chính của hệ thống Real Estate Management Platform.

---

## 👤 **User Journey 1: Khách Hàng Tìm Kiếm Bất Động Sản (Buyer/Renter)**

### Kịch bản: Một người dùng muốn tìm kiếm nhà để mua/thuê

```
┌─────────────────────────────────────────────────────────────┐
│ 1. User truy cập website                                     │
│    → Trang chủ hiển thị danh sách bất động sản phổ biến     │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 2. Chưa có tài khoản? → Đăng ký/Đăng nhập                 │
│    Endpoint: POST /api/auth/register                        │
│    Nhập: email, password, fullName, phone                  │
│    → Nhận OTP qua email                                     │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 3. Xác thực OTP                                             │
│    Endpoint: POST /api/auth/verify-otp                      │
│    Nhập: email, otp_code                                    │
│    → Nhận accessToken & refreshToken                        │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 4. Tìm kiếm & Lọc Bất Động Sản                            │
│    Endpoint: GET /api/properties?city=HCM&district=Q1&...   │
│    Filter: Thành phố, Quận, Giá, Diện tích, Phòng ngủ    │
│    → Nhận danh sách properties                              │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 5. Xem Chi Tiết Bất Động Sản                              │
│    Endpoint: GET /api/properties/{slug}                     │
│    → Xem hình ảnh, mô tả, giá, chủ sở hữu                 │
│    → View count được tăng lên +1                           │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 6. Thêm vào Danh Sách Yêu Thích (Optional)                │
│    Endpoint: POST /api/favorites/{propertyId}              │
│    → Property được lưu vào danh sách favorite              │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 7. Liên Hệ Chủ Nhà qua Chat                               │
│    Endpoint: POST /api/conversations                        │
│    → Tạo cuộc hội thoại với chủ nhà/agent                 │
│    → Có thể gửi tin nhắn trong thời gian thực (WebSocket) │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 8. Đặt Lịch Xem Nhà                                        │
│    Endpoint: POST /api/viewings                             │
│    Nhập: propertyId, viewingDate, note                     │
│    → Yêu cầu xem nhà được gửi đến chủ nhà (status: PENDING)│
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 9. Chờ Chủ Nhà Duyệt Lịch Hẹn                             │
│    Endpoint: GET /api/viewings/me                           │
│    → Xem trạng thái: PENDING → CONFIRMED → COMPLETED      │
│    → Nhận thông báo khi chủ nhà phê duyệt                  │
└─────────────────────────────────────────────────────────────┘
```

### Luồng Xử Lý:
1. **Tìm kiếm**: User sử dụng các filter để tìm kiếm properties phù hợp
2. **Xem chi tiết**: Click vào property để xem toàn bộ thông tin
3. **Lưu yêu thích**: Lưu properties yêu thích để xem lại sau
4. **Chat**: Liên lạc trực tiếp với chủ nhà/agent để trao đổi
5. **Đặt lịch**: Gửi yêu cầu xem nhà cho chủ nhà

---

## 🏠 **User Journey 2: Chủ Nhà Đăng Bài & Quản Lý Bất Động Sản**

### Kịch bản: Chủ nhà muốn đăng bài cho thuê/bán nhà

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Đăng ký và Đăng Nhập như User bình thường               │
│    → Nhận accessToken sau khi verify OTP                    │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 2. Nâng Cấp Tài Khoản lên Owner (Optional)                │
│    Endpoint: POST /api/roles/upgrade-to-owner              │
│    → Có thể bắt đầu đăng bài BDS                           │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 3. Tạo Bài Đăng Bất Động Sản                              │
│    Endpoint: POST /api/properties                           │
│    Nhập: title, description, price, area, bedrooms,        │
│          address, city, district, agentId (optional)       │
│    → Property được tạo với status: PENDING                 │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 4. Upload Hình Ảnh                                         │
│    Endpoint: POST /api/properties/{id}/images              │
│    Nhập: 1-10 hình ảnh                                      │
│    → Hình ảnh được lưu lên Cloudinary                      │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 5. Chờ Admin Phê Duyệt                                     │
│    Endpoint: GET /api/properties/{id}                       │
│    → Status: PENDING → APPROVED/REJECTED                   │
│    → Nếu rejected, admin sẽ gửi lý do từ chối             │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 6. Bài Đăng Được Duyệt & Hiển Thị Công Khai               │
│    → Visibility = true, Status = APPROVED                  │
│    → Bắt đầu nhận lượt xem từ users khác                   │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 7. Nhận Lịch Xem Nhà từ Khách Hàng                        │
│    Endpoint: GET /api/viewings (filtered by property)      │
│    → Nhận thông báo về viewing requests mới                │
│    → Xem hồ sơ người dùng muốn xem                         │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 8. Phê Duyệt hoặc Từ Chối Lịch Hẹn                        │
│    Endpoint: PATCH /api/viewings/{id}/status              │
│    → Status: PENDING → CONFIRMED/CANCELLED                │
│    → Gửi thông báo đến khách hàng                         │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 9. Chat với Khách Hàng                                     │
│    Endpoint: POST /api/conversations/{id}/messages         │
│    → Trao đổi chi tiết, thỏa thuận giá cả                 │
│    → Hỗ trợ tin nhắn thời gian thực qua WebSocket         │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 10. Quản Lý & Cập Nhật Bài Đăng                           │
│     Endpoint: PUT /api/properties/{id}                      │
│     → Cập nhật giá, mô tả, hình ảnh                        │
│     → Thay đổi trạng thái (AVAILABLE → SOLD → HIDDEN)     │
│     → Xem thống kê lượt xem, yêu thích                     │
│     Endpoint: GET /api/properties/{id}/analytics           │
└─────────────────────────────────────────────────────────────┘
```

### Luồng Xử Lý:
1. **Nâng cấp tài khoản**: Từ USER → OWNER
2. **Tạo bài đăng**: Nhập thông tin chi tiết bất động sản
3. **Upload ảnh**: Tải lên 1-10 hình ảnh (tối đa 10MB/ảnh)
4. **Chờ duyệt**: Admin review bài đăng, có thể approve/reject
5. **Quản lý**: Cập nhật thông tin, xem thống kê, quản lý lịch hẹn
6. **Xử lý requests**: Phê duyệt/từ chối yêu cầu xem nhà từ khách

---

## 🎓 **User Journey 3: Môi Giới Nâng Cấp Tài Khoản**

### Kịch bản: Một người dùng muốn trở thành môi giới bất động sản

```
┌─────────────────────────────────────────────────────────────┐
│ 1. User Đã Có Tài Khoản                                     │
│    → Role hiện tại: USER                                    │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 2. Gửi Yêu Cầu Nâng Cấp lên Agent                         │
│    Endpoint: POST /api/roles/upgrade-to-agent              │
│    Nhập: content (giới thiệu bản thân, kinh nghiệm)       │
│    → AgentRequest được tạo với status: PENDING             │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 3. Chờ Admin Xét Duyệt                                     │
│    → Admin xem danh sách agent requests                    │
│    → Endpoint: GET /api/roles/agent-requests               │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 4. Admin Phê Duyệt hoặc Từ Chối                           │
│    Approve:                                                 │
│    Endpoint: PATCH /api/roles/agent-requests/{id}/approve  │
│    → User Role được cập nhật: USER → AGENT                 │
│    → User nhận thông báo phê duyệt                         │
│                                                             │
│    Reject:                                                  │
│    Endpoint: PATCH /api/roles/agent-requests/{id}/reject   │
│    → User nhận thông báo từ chối & lý do                   │
│    → User vẫn giữ role USER                                │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 5. Agent Bắt Đầu Hoạt Động                                │
│    (Nếu phê duyệt)                                         │
│    → Có thể đăng bài BDS của chính mình                    │
│    → Có thể quản lý BDS cho chủ nhà (agentId)             │
│    → Nhận hoa hồng từ các giao dịch                       │
│    → Có quyền truy cập các API dành riêng cho Agent       │
└─────────────────────────────────────────────────────────────┘
```

### Luồng Xử Lý:
1. **Gửi yêu cầu**: User điền nội dung yêu cầu nâng cấp
2. **Chờ duyệt**: Admin xem xét và quyết định
3. **Phê duyệt/Từ chối**: Admin gửi kết quả cho user
4. **Bắt đầu hoạt động**: Nếu được duyệt, user có thể đăng bài & quản lý

---

## 👨‍💼 **User Journey 4: Admin Quản Lý Hệ Thống**

### Kịch bản: Admin cấu hình, duyệt bài, và quản lý hệ thống

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Admin Đăng Nhập                                          │
│    → Tài khoản admin được tạo sẵn trong database            │
│    Endpoint: POST /api/auth/login                           │
│    → Role: ADMIN                                            │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 2. Duyệt Bài Đăng Bất Động Sản (Moderation)              │
│    Endpoint: GET /api/properties?status=PENDING             │
│    → Xem danh sách bài chờ duyệt                           │
│    → Kiểm tra thông tin, hình ảnh                          │
│                                                             │
│    Phê duyệt:                                               │
│    Endpoint: PATCH /api/properties/{id}/status             │
│    Body: { status: "APPROVED", rejectionReason: null }     │
│    → Bài đăng được hiển thị công khai                      │
│                                                             │
│    Từ chối:                                                 │
│    Endpoint: PATCH /api/properties/{id}/status             │
│    Body: { status: "REJECTED",                              │
│            rejectionReason: "Thông tin không chính xác" }  │
│    → Chủ nhà nhận thông báo & có thể sửa                   │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 3. Xét Duyệt Yêu Cầu Nâng Cấp Agent                       │
│    Endpoint: GET /api/roles/agent-requests?status=PENDING   │
│    → Xem danh sách agent requests                          │
│    → Kiểm tra hồ sơ người dùng                             │
│                                                             │
│    Approve:                                                 │
│    Endpoint: PATCH /api/roles/agent-requests/{id}/approve   │
│    → User Role: USER → AGENT                               │
│                                                             │
│    Reject:                                                  │
│    Endpoint: PATCH /api/roles/agent-requests/{id}/reject   │
│    → User vẫn là USER, nhận lý do từ chối                  │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 4. Quản Lý Người Dùng                                      │
│    Endpoint: GET /api/users/search?keyword=...             │
│    → Tìm kiếm users theo email, tên                        │
│    → Xem thông tin chi tiết user                           │
│    → Có thể vô hiệu hóa tài khoản spam/vi phạm            │
│    → Xem lịch sử hoạt động                                 │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 5. Xem Thống Kê & Báo Cáo                                 │
│    → Số lượng users, properties, viewings                  │
│    → Tổng doanh thu từ agent commissions                   │
│    → Properties phổ biến nhất                              │
│    → User activity metrics                                 │
└────────────────────────┬──────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│ 6. Xử Lý Báo Cáo Vi Phạm (Optional)                       │
│    → Nhận báo cáo từ users về listings hoặc users          │
│    → Điều tra và xử lý vi phạm                             │
│    → Có thể xóa bài đăng hoặc khóa tài khoản              │
└─────────────────────────────────────────────────────────────┘
```

### Luồng Xử Lý:
1. **Duyệt bài đăng**: Review thông tin, phê duyệt hoặc từ chối
2. **Duyệt agent requests**: Xem xét & phê duyệt/từ chối nâng cấp
3. **Quản lý users**: Tìm kiếm, xem thông tin, xử lý vi phạm
4. **Thống kê**: Xem báo cáo hệ thống

---

## 🔐 **Security Flow: JWT Authentication & Token Refresh**

```
┌──────────────────────────────────────────────────────────┐
│ Client đăng nhập                                          │
│ POST /api/auth/login                                      │
│ { email, password }                                       │
└─────────────────────┬──────────────────────────────────┘
                      │
         ┌────────────▼────────────┐
         │ Server xác minh email   │
         │ & password              │
         └────────────┬────────────┘
                      │
         ┌────────────▼────────────┐
         │ Tạo JWT Access Token    │
         │ (hết hạn: 1 giờ)        │
         │                         │
         │ Tạo Refresh Token       │
         │ (hết hạn: 7 ngày)       │
         │ Lưu vào DB              │
         └────────────┬────────────┘
                      │
         ┌────────────▼────────────────────────────────┐
         │ Response: {                                  │
         │   accessToken: "eyJ...",                    │
         │   refreshToken: "eyJ...",                   │
         │   expiresIn: 3600                           │
         │ }                                            │
         └────────────┬────────────────────────────────┘
                      │
         ┌────────────▼────────────┐
         │ Client lưu tokens       │
         │ (localStorage/cookies)  │
         └────────────┬────────────┘
                      │
         ┌────────────▼────────────┐
         │ Client gọi API protected│
         │ Header: Authorization   │
         │ Bearer {accessToken}    │
         └────────────┬────────────┘
                      │
         ┌────────────▼────────────┐
         │ Server kiểm tra token   │
         │ Hợp lệ → Xử lý request │
         │ Hết hạn → 401 Error    │
         └────────────┬────────────┘
                      │
         ┌────────────▼────────────┐
         │ Access Token hết hạn?   │
         │ Nếu có → Refresh Token  │
         └────────────┬────────────┘
                      │
         ┌────────────▼────────────┐
         │ POST /api/auth/refresh  │
         │ { refreshToken }        │
         └────────────┬────────────┘
                      │
         ┌────────────▼────────────┐
         │ Server xác minh refresh │
         │ token & tạo cặp token   │
         │ mới                     │
         └────────────┬────────────┘
                      │
         ┌────────────▼────────────────┐
         │ Response: {                  │
         │   accessToken: "eyJ...",    │
         │   refreshToken: "eyJ...",   │
         │   expiresIn: 3600           │
         │ }                            │
         └──────────────────────────────┘
```

---

## 📱 **WebSocket Real-time Communication Flow**

```
Client A (User)              WebSocket Server              Client B (User)
     │                             │                             │
     │─ Connect & auth token ─────►│                             │
     │                             │ Validate JWT                │
     │                             │                             │
     │◄────── Connection OK ───────│                             │
     │                             │                             │
     │ Send Message                │                             │
     │─ { type: "NEW_MESSAGE" }───►│                             │
     │                             │ Broadcast to Client B       │
     │                             ├────── NEW_MESSAGE ─────────►│
     │                             │                             │
     │◄───── User Online Status ───│ Mark Client A as active     │
     │       (Presence event)      │                             │
     │                             │                             │
     │                      Socket closes                        │
     │ User goes offline           │ Mark as inactive           │
     │─ Connection closed ────────►│                             │
     │                             │ Broadcast offline status   │
     │                             ├────── USER_OFFLINE ──────►│
```

---

## 🔄 **Data Consistency & Transaction Handling**

### Khi User Thêm vào Favorites:
1. Kiểm tra property có tồn tại
2. Kiểm tra user chưa yêu thích property này
3. Tạo record favorites
4. Nếu có lỗi → Rollback transaction

### Khi Cập Nhật Property:
1. Kiểm tra owner có quyền
2. Cập nhật thông tin property
3. Reset status → PENDING (vì đã thay đổi)
4. Thông báo cho admin review lại

### Khi Phê Duyệt Agent Request:
1. Kiểm tra request status = PENDING
2. Cập nhật Agent Request → APPROVED
3. Tạo Agent profile cho user
4. Cập nhật User role → AGENT
5. Gửi email thông báo

---

## ✅ **Validation & Error Handling**

### Email Validation:
- Format phải hợp lệ (RFC 5322)
- Phải là email duy nhất trong hệ thống
- Kiểm tra email tồn tại (optional)

### Password Validation:
- Tối thiểu 8 ký tự
- Phải chứa ít nhất: 1 chữ hoa, 1 chữ thường, 1 số, 1 ký tự đặc biệt
- Không được trùng email
- Phải hash bằng BCrypt trước khi lưu

### Property Validation:
- Title: 10-255 ký tự
- Price: > 0
- Area: > 0
- Address, City, District: không được rỗng
- Images: 1-10 files, mỗi file ≤ 10MB

### Viewing Date Validation:
- Phải là ngày trong tương lai
- Không được đặt lịch < 1 giờ

---

## 🚨 **Error Scenarios**

| Scenario | HTTP Code | Error Message |
|----------|-----------|---------------|
| Email đã tồn tại | 409 | Email already exists |
| Password sai | 401 | Invalid email or password |
| Token hết hạn | 401 | Token expired |
| Không có quyền | 403 | Access denied |
| Resource không tồn tại | 404 | Resource not found |
| Dữ liệu không hợp lệ | 400 | Validation error: ... |
| Server error | 500 | Internal server error |


