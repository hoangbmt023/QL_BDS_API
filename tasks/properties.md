# PROPERTY WORKFLOW

## Roles trong hệ thống

| Role | Nhiệm vụ |
|---|---|
| USER | Xem property, favorite, viewing, report |
| OWNER | Chủ sở hữu bất động sản |
| AGENT | Môi giới / quản lý listing |
| MODERATOR | Kiểm duyệt property |
| ADMIN | Quản trị toàn hệ thống |

---

# OWNER & AGENT FLOW

## OWNER
OWNER là chủ sở hữu thật của bất động sản.

### OWNER có thể:
- [x] Tạo property của chính mình
- [x] Upload ảnh property
- [x] Chỉnh sửa property
- [x] Xóa property
- [x] Đánh dấu SOLD / RENTED
- [ ] Xem viewing appointments
- [ ] Chat với khách hàng

### OWNER KHÔNG:
- [ ] Duyệt bài viết
- [ ] Ẩn bài viết
- [ ] Quản trị hệ thống

---

## AGENT
AGENT là môi giới hoặc người đại diện đăng bài cho OWNER.

### AGENT có thể:
- [x] Đăng property cho OWNER
- [x] Quản lý nhiều listing
- [x] Upload ảnh property
- [x] Chỉnh sửa property
- [ ] Quản lý viewing
- [ ] Chat với khách hàng
- [x] Theo dõi hiệu quả listing

### AGENT KHÔNG:
- [ ] Duyệt bài viết
- [ ] Quản trị hệ thống

---

# PROPERTY RELATIONSHIP

## Property được liên kết như sau

```text
Property
- owner_id
- agent_id (nullable)
```

### Trường hợp OWNER tự đăng:
```text
owner_id = OWNER
agent_id = null
```

### Trường hợp AGENT đăng cho OWNER:
```text
owner_id = OWNER
agent_id = AGENT
```

---

# PROPERTY WORKFLOW

## 1. Luồng tạo tin đăng bất động sản

### OWNER tự đăng
- [ ] OWNER đăng nhập hệ thống
- [x] Tạo property mới
- [x] Nhập thông tin property:
  - tiêu đề
  - mô tả
  - giá
  - diện tích
  - địa chỉ
  - số phòng
  - tiện ích
- [x] Upload nhiều ảnh property
- [x] Lưu danh sách ảnh vào `property_images`
- [x] Lưu:
  - `owner_id`
  - `agent_id = null`
- [x] Property mặc định có status = `PENDING`
- [x] Chờ MODERATOR duyệt

### AGENT đăng cho OWNER
- [ ] AGENT đăng nhập hệ thống
- [ ] Chọn OWNER
- [x] Tạo property mới
- [x] Upload ảnh property
- [x] Lưu:
  - `owner_id`
  - `agent_id`
- [x] Property mặc định có status = `PENDING`
- [x] Chờ MODERATOR duyệt

---

## 2. Luồng duyệt tin đăng
- [x] MODERATOR xem danh sách property đang `PENDING`
- [x] Kiểm tra:
  - nội dung
  - hình ảnh
  - giá
  - thông tin liên hệ
- [x] Duyệt hoặc từ chối bài viết

### APPROVE
- [x] Đổi status = `APPROVED`
- [x] Property hiển thị công khai

### REJECT
- [x] Đổi status = `REJECTED`
- [x] Trả lý do từ chối

---

## 3. Luồng hiển thị property
- [x] USER truy cập danh sách property
- [x] Chỉ hiển thị property có status = `APPROVED`
- [x] USER xem chi tiết property
- [x] Hiển thị:
  - ảnh
  - giá
  - diện tích
  - địa chỉ
  - owner/agent
  - tiện ích
  - thông tin cơ bản về bất động sản

---

## 4. Luồng chỉnh sửa property
- [x] OWNER hoặc AGENT chỉnh sửa property
- [x] Cập nhật thông tin property
- [x] Có thể yêu cầu duyệt lại
- [x] Property quay về status = `PENDING`

---

## 5. Luồng ẩn bài viết vi phạm
- [ ] USER report property
- [ ] Tạo `property_report`
- [x] MODERATOR review report
- [x] Nếu vi phạm:
  - đổi status = `HIDDEN`
  - lưu lý do ẩn bài

---

## 6. Luồng restore bài viết
- [x] OWNER hoặc AGENT sửa nội dung vi phạm
- [x] Gửi yêu cầu mở lại bài
- [x] MODERATOR kiểm tra lại
- [x] Nếu hợp lệ:
  - đổi status = `APPROVED`

---

## 7. Luồng khóa property
- [x] OWNER hoặc AGENT đánh dấu:
  - SOLD
  - RENTED
- [x] Property không còn hiển thị public

---

## 8. Luồng xóa property
- [x] OWNER hoặc AGENT xóa property
- [x] Không hard delete
- [x] Chuyển sang:
  - `DELETED`
  - hoặc `HIDDEN`

---

## 9. Luồng tìm kiếm property
- [x] USER tìm kiếm property
- [x] Filter theo:
  - địa điểm
  - khoảng giá
  - số phòng
  - diện tích
  - tiện ích
- [x] Hỗ trợ pagination
- [x] Hỗ trợ sorting

---

## 10. Luồng gợi ý property tương tự
- [x] USER xem chi tiết property
- [x] Backend tìm property tương tự:
  - cùng khu vực
  - cùng mức giá
  - cùng loại
  - gần số phòng
- [x] Trả danh sách related properties

---

## 11. Luồng thống kê hiệu quả listing
- [x] ADMIN hoặc MODERATOR xem analytics
- [x] Thống kê:
  - lượt xem
  - số lượt favorite
  - số lịch hẹn viewing
  - số conversation
  - conversion rate

---

# PROPERTY STATUS

- [x] PENDING
- [x] APPROVED
- [x] REJECTED
- [x] HIDDEN
- [x] SOLD
- [x] RENTED
- [x] DELETED

---

# VIEWING STATUS

- [ ] PENDING
- [ ] CONFIRMED
- [ ] CANCELLED
- [ ] COMPLETED