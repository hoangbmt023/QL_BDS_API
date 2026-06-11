# 🧪 Tài Liệu Kiểm Thử (Testing Documentation)

Dự án sử dụng **JUnit 5** và **Mockito** để viết Unit Tests và Integration Tests. Tài liệu này mô tả chiến lược kiểm thử và các test cases chính.

---

## 📊 Chiến Lược Kiểm Thử

```
┌─────────────────────────────────────────┐
│        Test Pyramid Strategy            │
├─────────────────────────────────────────┤
│                                         │
│              UI Tests (E2E)             │  5%
│              (Selenium/Cypress)         │
│                                         │
│           Integration Tests             │  15%
│           (API Tests with @SpringBootTest│
│           Database, Real Dependencies)  │
│                                         │
│            Unit Tests                   │  80%
│            (@ExtendWith(MockitoExtension│
│            Mocked Dependencies)         │
│                                         │
└─────────────────────────────────────────┘
```

### Tỷ Lệ Coverage:
- **Branches**: 75%+
- **Lines**: 80%+
- **Classes**: 85%+

---

## 🚀 Cách Chạy Tests

### Chạy Tất Cả Tests:
```bash
mvn test
```

### Chạy Tests Cho Một Service:
```bash
mvn test -Dtest=com.example.qlbds.auth_service.*Test
mvn test -Dtest=com.example.qlbds.property_service.*Test
```

### Chạy Test Cụ Thể:
```bash
mvn test -Dtest=AuthServiceImplTest#testLogin_ShouldReturnTokens_WhenCredentialsAreValid
```

### Chạy Tests Với Coverage Report:
```bash
mvn clean test jacoco:report
# Report được sinh ra tại: target/site/jacoco/index.html
```

### Chạy Tests & Skip Build:
```bash
mvn test -DskipTests=false
```

---

## 📋 Các Test Cases Chính

### **1️⃣ Authentication Service Tests** (`auth_service`)

#### TC01: Đăng ký thành công
```java
@Test
void register_ShouldCreateNewUser_WhenEmailNotExists() {
    // Arrange
    RegisterRequest request = new RegisterRequest(
        "newuser@example.com", "Password123!", "Nguyễn Văn A", "0912345678"
    );
    
    // Act
    AuthResponse response = authService.register(request);
    
    // Assert
    assertNotNull(response);
    assertEquals("newuser@example.com", response.getEmail());
    verify(emailService).sendOtpEmail(eq("newuser@example.com"), anyString());
}
```

**Mục tiêu**: Kiểm tra việc tạo tài khoản mới, gửi OTP qua email

---

#### TC02: Đăng ký thất bại - Email đã tồn tại
```java
@Test
void register_ShouldThrowException_WhenEmailAlreadyExists() {
    // Arrange
    RegisterRequest request = new RegisterRequest(
        "existing@example.com", "Password123!", "User", "0912345678"
    );
    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
    
    // Act & Assert
    assertThrows(DuplicateResourceException.class, () -> authService.register(request));
}
```

**Mục tiêu**: Đảm bảo không cho phép đăng ký với email đã tồn tại

---

#### TC03: Xác thực OTP thành công
```java
@Test
void verifyOtp_ShouldReturnTokens_WhenOtpIsValid() {
    // Arrange
    Otp validOtp = new Otp();
    validOtp.setEmail("user@example.com");
    validOtp.setOtpCode("123456");
    validOtp.setIsVerified(false);
    validOtp.setExpiryTime(LocalDateTime.now().plusMinutes(15));
    
    when(otpRepository.findByEmailAndIsVerifiedFalse("user@example.com"))
        .thenReturn(Optional.of(validOtp));
    when(userRepository.findByEmail("user@example.com"))
        .thenReturn(Optional.of(user));
    
    // Act
    AuthResponse response = authService.verifyOtp("user@example.com", "123456");
    
    // Assert
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());
    assertTrue(validOtp.getIsVerified());
}
```

**Mục tiêu**: Kiểm tra xác thực OTP & cấp tokens

---

#### TC04: Đăng nhập thành công
```java
@Test
void login_ShouldReturnTokens_WhenCredentialsAreValid() {
    // Arrange
    LoginRequest request = new LoginRequest("user@example.com", "Password123!");
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("Password123!", user.getPassword())).thenReturn(true);
    
    // Act
    AuthResponse response = authService.login(request);
    
    // Assert
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());
    verify(refreshTokenRepository).save(any(RefreshToken.class));
}
```

**Mục tiêu**: Kiểm tra đăng nhập & cấp JWT tokens

---

#### TC05: Đăng nhập thất bại - Password sai
```java
@Test
void login_ShouldThrowException_WhenPasswordIsWrong() {
    // Arrange
    LoginRequest request = new LoginRequest("user@example.com", "WrongPassword");
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("WrongPassword", user.getPassword())).thenReturn(false);
    
    // Act & Assert
    assertThrows(InvalidResourceException.class, () -> authService.login(request));
}
```

**Mục tiêu**: Đảm bảo không đăng nhập được với password sai

---

#### TC06: OTP Rate Limiting
```java
@Test
void resendOtp_ShouldThrowException_WhenExceedingRateLimit() {
    // Arrange
    for (int i = 0; i < 5; i++) {
        authService.resendOtp("user@example.com");
    }
    
    // Act & Assert - 6th request should fail
    assertThrows(RateLimitExceededException.class, 
        () -> authService.resendOtp("user@example.com"));
}
```

**Mục tiêu**: Kiểm tra rate limiting cho OTP (max 5 requests/hour)

---

### **2️⃣ User Service Tests** (`user_service`)

#### TC07: Nâng cấp lên Owner
```java
@Test
void upgradeToOwner_ShouldSetRoleToOwner_WhenUserExists() {
    // Arrange
    User user = new User();
    user.setId(1L);
    user.setRole(UserRole.USER);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    
    // Act
    userService.upgradeToOwner(1L);
    
    // Assert
    assertEquals(UserRole.OWNER, user.getRole());
    verify(userRepository).save(user);
}
```

**Mục tiêu**: Kiểm tra nâng cấp tài khoản lên Owner

---

#### TC08: Gửi yêu cầu trở thành Agent
```java
@Test
void submitAgentRequest_ShouldCreatePendingRequest() {
    // Arrange
    AgentRequestDto request = new AgentRequestDto("Tôi muốn trở thành agent...");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    
    // Act
    AgentRequestResponse response = roleService.submitAgentRequest(1L, request);
    
    // Assert
    assertEquals(AgentRequestStatus.PENDING, response.getStatus());
    verify(agentRequestRepository).save(any(AgentRequest.class));
}
```

**Mục tiêu**: Kiểm tra gửi yêu cầu nâng cấp Agent

---

#### TC09: Admin phê duyệt Agent Request
```java
@Test
void approveAgentRequest_ShouldUpgradeUserRoleToAgent() {
    // Arrange
    AgentRequest request = new AgentRequest();
    request.setId(1L);
    request.setUser(user);
    request.setStatus(AgentRequestStatus.PENDING);
    
    when(agentRequestRepository.findById(1L)).thenReturn(Optional.of(request));
    
    // Act
    roleService.approveAgentRequest(1L, admin);
    
    // Assert
    assertEquals(AgentRequestStatus.APPROVED, request.getStatus());
    assertEquals(UserRole.AGENT, user.getRole());
    verify(agentRequestRepository).save(request);
    verify(emailService).sendApprovalEmail(eq(user.getEmail()), anyString());
}
```

**Mục tiêu**: Kiểm tra phê duyệt yêu cầu Agent bởi Admin

---

#### TC10: Cập nhật hồ sơ người dùng
```java
@Test
void updateProfile_ShouldUpdateUserInfo() {
    // Arrange
    UpdateProfileRequest request = new UpdateProfileRequest(
        "Nguyễn Văn B", "0987654321", "https://..."
    );
    
    // Act
    UserResponse response = userService.updateProfile(1L, request);
    
    // Assert
    assertEquals("Nguyễn Văn B", response.getFullName());
    assertEquals("0987654321", response.getPhone());
    verify(userRepository).save(user);
}
```

**Mục tiêu**: Kiểm tra cập nhật hồ sơ người dùng

---

### **3️⃣ Property Service Tests** (`property_service`)

#### TC11: Tạo bất động sản
```java
@Test
void create_ShouldCreateProperty_WhenRequestIsValid() {
    // Arrange
    CreatePropertyRequest request = new CreatePropertyRequest(
        "Nhà phố 3 tầng", "Mô tả...", new BigDecimal("5000000000"),
        100.5, 3, 2, "123 Nguyễn Huệ", "HCM", "Q1", ownerId, null
    );
    Owner owner = new Owner();
    owner.setId(1L);
    
    when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
    when(slugService.toSlug("Nhà phố 3 tầng")).thenReturn("nha-pho-3-tang");
    when(propertyRepository.save(any(Property.class))).thenReturn(property);
    
    // Act
    PropertyResponse response = propertyService.create(request);
    
    // Assert
    assertNotNull(response);
    assertEquals(PropertyStatus.PENDING, response.getStatus());
    verify(propertyRepository).save(any(Property.class));
}
```

**Mục tiêu**: Kiểm tra tạo bài đăng bất động sản

---

#### TC12: Lọc bất động sản theo quận và giá
```java
@Test
void find_ShouldReturnFilteredProperties() {
    // Arrange
    PropertySpecification spec = new PropertySpecification(
        "Q1", new BigDecimal("1000000000"), new BigDecimal("10000000000"), null
    );
    List<Property> expected = List.of(property);
    
    when(propertyRepository.findAll(eq(spec), any(Pageable.class)))
        .thenReturn(new PageImpl<>(expected));
    
    // Act
    Page<PropertyResponse> results = propertyService.findProperties(spec, 0, 20);
    
    // Assert
    assertEquals(1, results.getContent().size());
    assertEquals("test-property", results.getContent().get(0).getSlug());
}
```

**Mục tiêu**: Kiểm tra lọc bất động sản theo tiêu chí

---

#### TC13: Cập nhật bất động sản reset status
```java
@Test
void update_ShouldResetStatusToPending() {
    // Arrange
    UpdatePropertyRequest request = new UpdatePropertyRequest(
        "Updated Title", "Updated Desc", new BigDecimal("6000000000"),
        120.0, 3, 2, "456 Nguyễn Huệ", "HCM", "Q1"
    );
    property.setStatus(PropertyStatus.APPROVED);
    property.setVisibility(true);
    
    when(propertyRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));
    
    // Act
    propertyService.update(1L, request);
    
    // Assert
    assertEquals(PropertyStatus.PENDING, property.getStatus());
    assertFalse(property.getVisibility());
    assertNull(property.getRejectionReason());
}
```

**Mục tiêu**: Kiểm tra cập nhật BDS reset status về PENDING

---

#### TC14: Upload hình ảnh
```java
@Test
void uploadImages_ShouldUploadAndSaveImages() throws IOException {
    // Arrange
    MultipartFile file1 = mock(MultipartFile.class);
    MultipartFile file2 = mock(MultipartFile.class);
    List<MultipartFile> files = List.of(file1, file2);
    
    when(propertyRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));
    when(fileUploadService.uploadMultipleFiles(files, "properties/1"))
        .thenReturn(List.of("url1", "url2"));
    
    // Act
    propertyService.uploadImages(1L, files);
    
    // Assert
    verify(propertyImageRepository).saveAll(argThat(images -> images.size() == 2));
}
```

**Mục tiêu**: Kiểm tra upload hình ảnh lên Cloudinary

---

#### TC15: Cập nhật hình ảnh - Kiểm tra quyền sở hữu
```java
@Test
void updateImage_ShouldThrowException_WhenImageNotBelongToProperty() {
    // Arrange
    PropertyImage image = new PropertyImage();
    image.setId(10L);
    Property otherProperty = new Property();
    otherProperty.setId(99L);
    image.setProperty(otherProperty);
    
    when(propertyImageRepository.findById(10L)).thenReturn(Optional.of(image));
    
    // Act & Assert
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> propertyService.updateImage(1L, 10L, newFile));
    assertTrue(ex.getMessage().contains("không thuộc về bất động sản này"));
}
```

**Mục tiêu**: Đảm bảo owner chỉ có thể cập nhật ảnh của property của mình

---

#### TC16: Admin phê duyệt bất động sản
```java
@Test
void changeStatus_ShouldApprovePropertyAndSetVisibilityTrue() {
    // Arrange
    property.setStatus(PropertyStatus.PENDING);
    property.setVisibility(false);
    
    when(propertyRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));
    
    // Act
    propertyService.changeStatus(1L, PropertyStatus.APPROVED, null);
    
    // Assert
    assertEquals(PropertyStatus.APPROVED, property.getStatus());
    assertTrue(property.getVisibility());
    assertNull(property.getRejectionReason());
}
```

**Mục tiêu**: Kiểm tra phê duyệt bài đăng bởi Admin

---

#### TC17: Admin từ chối bài đăng
```java
@Test
void changeStatus_ShouldRejectPropertyAndSetVisibilityFalse() {
    // Arrange
    property.setStatus(PropertyStatus.PENDING);
    
    when(propertyRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));
    
    // Act
    propertyService.changeStatus(1L, PropertyStatus.REJECTED, "Thông tin không chính xác");
    
    // Assert
    assertEquals(PropertyStatus.REJECTED, property.getStatus());
    assertFalse(property.getVisibility());
    assertEquals("Thông tin không chính xác", property.getRejectionReason());
}
```

**Mục tiêu**: Kiểm tra từ chối bài đăng với lý do

---

### **4️⃣ Viewing Service Tests** (`viewing_service`)

#### TC18: Đặt lịch xem nhà
```java
@Test
void createViewing_ShouldCreatePendingViewing() {
    // Arrange
    CreateViewingRequest request = new CreateViewingRequest(
        propertyId, LocalDateTime.now().plusDays(7), "Muốn xem vào chiều"
    );
    
    when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    
    // Act
    ViewingResponse response = viewingService.createViewing(userId, request);
    
    // Assert
    assertEquals(ViewingStatus.PENDING, response.getStatus());
    verify(viewingRepository).save(any(Viewing.class));
}
```

**Mục tiêu**: Kiểm tra đặt lịch xem nhà

---

#### TC19: Xác thực lịch hẹn trong tương lai
```java
@Test
void createViewing_ShouldThrowException_WhenDateIsInPast() {
    // Arrange
    CreateViewingRequest request = new CreateViewingRequest(
        propertyId, LocalDateTime.now().minusDays(1), "Note"
    );
    
    // Act & Assert
    assertThrows(InvalidResourceException.class,
        () -> viewingService.createViewing(userId, request));
}
```

**Mục tiêu**: Đảm bảo không đặt lịch hẹn trong quá khứ

---

#### TC20: Owner phê duyệt lịch hẹn
```java
@Test
void updateViewingStatus_ShouldConfirmViewing() {
    // Arrange
    viewing.setStatus(ViewingStatus.PENDING);
    viewing.setProperty(property);
    property.setOwnerId(ownerId);
    
    when(viewingRepository.findById(viewingId)).thenReturn(Optional.of(viewing));
    
    // Act
    viewingService.updateStatus(viewingId, ViewingStatus.CONFIRMED, ownerId);
    
    // Assert
    assertEquals(ViewingStatus.CONFIRMED, viewing.getStatus());
    verify(viewingRepository).save(viewing);
}
```

**Mục tiêu**: Kiểm tra phê duyệt lịch hẹn bởi owner

---

#### TC21: Người dùng khác không thể phê duyệt
```java
@Test
void updateViewingStatus_ShouldThrowException_WhenNotOwner() {
    // Arrange
    viewing.setProperty(property);
    property.setOwnerId(otherOwnerId);
    
    when(viewingRepository.findById(viewingId)).thenReturn(Optional.of(viewing));
    
    // Act & Assert
    assertThrows(AccessDeniedException.class,
        () -> viewingService.updateStatus(viewingId, ViewingStatus.CONFIRMED, currentUserId));
}
```

**Mục tiêu**: Đảm bảo chỉ owner BDS mới có thể phê duyệt lịch

---

### **5️⃣ Conversation & Message Tests** (`conversation_service`)

#### TC22: Tạo cuộc hội thoại
```java
@Test
void createConversation_ShouldCreateOrReturnExisting() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));
    when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
    
    // Act
    ConversationResponse response = conversationService.createOrGetConversation(
        userId, recipientId, propertyId
    );
    
    // Assert
    assertNotNull(response.getId());
    assertEquals(propertyId, response.getPropertyId());
}
```

**Mục tiêu**: Kiểm tra tạo cuộc hội thoại

---

#### TC23: Gửi tin nhắn
```java
@Test
void sendMessage_ShouldCreateMessageAndBroadcastViaWebSocket() {
    // Arrange
    SendMessageRequest request = new SendMessageRequest("Xin chào!");
    
    when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
    when(userRepository.findById(senderId)).thenReturn(Optional.of(user));
    
    // Act
    MessageResponse response = conversationService.sendMessage(conversationId, senderId, request);
    
    // Assert
    assertEquals("Xin chào!", response.getContent());
    assertFalse(response.isRead());
    verify(messagingTemplate).convertAndSend(anyString(), any());
}
```

**Mục tiêu**: Kiểm tra gửi tin nhắn & broadcast WebSocket

---

#### TC24: Người dùng khác không thể gửi tin trong cuộc hội thoại
```java
@Test
void sendMessage_ShouldThrowException_WhenUserNotInConversation() {
    // Arrange
    SendMessageRequest request = new SendMessageRequest("Message");
    conversation.setUser1(user1);
    conversation.setUser2(user2);
    
    when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
    
    // Act & Assert
    assertThrows(AccessDeniedException.class,
        () -> conversationService.sendMessage(conversationId, user3.getId(), request));
}
```

**Mục tiêu**: Đảm bảo chỉ participants của conversation mới có thể gửi tin

---

### **6️⃣ Favorite Service Tests** (`favorite_service`)

#### TC25: Thêm vào yêu thích
```java
@Test
void addToFavorite_ShouldCreateFavorite() {
    // Arrange
    when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(favoriteRepository.existsByUserIdAndPropertyId(userId, propertyId)).thenReturn(false);
    
    // Act
    favoriteService.toggleFavorite(userId, propertyId);
    
    // Assert
    verify(favoriteRepository).save(any(Favorite.class));
}
```

**Mục tiêu**: Kiểm tra thêm BDS vào yêu thích

---

#### TC26: Xóa khỏi yêu thích
```java
@Test
void removeFavorite_ShouldDeleteFavorite_WhenAlreadyFavorited() {
    // Arrange
    Favorite favorite = new Favorite();
    when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
    when(favoriteRepository.findByUserIdAndPropertyId(userId, propertyId))
        .thenReturn(Optional.of(favorite));
    
    // Act
    favoriteService.toggleFavorite(userId, propertyId);
    
    // Assert
    verify(favoriteRepository).delete(favorite);
}
```

**Mục tiêu**: Kiểm tra xóa BDS khỏi yêu thích (toggle)

---

#### TC27: Xóa nhiều yêu thích cùng lúc
```java
@Test
void bulkDeleteFavorites_ShouldDeleteMultipleFavorites() {
    // Arrange
    List<Long> propertyIds = List.of(1L, 2L, 3L);
    
    // Act
    favoriteService.bulkDeleteFavorites(userId, propertyIds);
    
    // Assert
    verify(favoriteRepository, times(3)).delete(any(Favorite.class));
}
```

**Mục tiêu**: Kiểm tra xóa nhiều yêu thích cùng lúc

---

## 🎯 Test Coverage Goals

| Module | Target | Current |
|--------|--------|---------|
| auth_service | 85% | 82% |
| user_service | 80% | 78% |
| property_service | 80% | 75% |
| viewing_service | 80% | 72% |
| conversation_service | 75% | 70% |
| favorite_service | 75% | 68% |
| **Overall** | **80%** | **74%** |

---

## 📈 Test Execution Pipeline

```
┌──────────────────┐
│  Git Push        │
└────────┬─────────┘
         │
┌────────▼─────────┐
│  Run Unit Tests  │  (mvn test)
│  (Mockito)       │
└────────┬─────────┘
         │
      ┌──┴─────────────────┐
      │ All Pass?          │
      └──┬───────────────┬──┘
         │ NO            │ YES
    ┌────▼──────┐   ┌────▼──────────────┐
    │ Fail Build │   │ Code Coverage     │
    └───────────┘   │ (jacoco:report)   │
                    └────┬──────────────┘
                         │
                      ┌──┴─────────────────┐
                      │ Coverage > 75%?    │
                      └──┬──────────────┬──┘
                         │ NO           │ YES
                     ┌────▼──────┐  ┌────▼──────────────┐
                     │ Warn      │  │ Integration Tests │
                     │ (Build ok)│  │ (@SpringBootTest)│
                     └───────────┘  └────┬──────────────┘
                                         │
                                    ┌────▼──────────┐
                                    │ Deploy Staging │
                                    └────────────────┘
```

---

## 🔧 Test Annotations & Setup

### JUnit 5 Annotations:
```java
@ExtendWith(MockitoExtension.class)  // Enable Mockito
@SpringBootTest                       // Integration test
@DataJpaTest                          // JPA repository test
@WebMvcTest(MyController.class)       // Controller test
```

### Common Test Methods:
```java
@BeforeEach     // Setup trước mỗi test
@AfterEach      // Cleanup sau mỗi test
@BeforeAll      // Setup 1 lần trước tất cả tests
@AfterAll       // Cleanup 1 lần sau tất cả tests
@ParameterizedTest  // Run test với multiple parameters
```

---

## ⚠️ Common Test Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| NullPointerException | Mock chưa initialized | Thêm `@ExtendWith(MockitoExtension.class)` |
| Assertion failed | Expected ≠ Actual | Kiểm tra setup data & mock expectations |
| Mock not called | Verify sai | Dùng `verify(mock).method()` đúng cách |
| Test timeout | Infinite loop/wait | Thêm `timeout` parameter, fix logic |
| Database locked | Concurrent test | Sử dụng `@Transactional` hoặc lock |

---

## 📚 Best Practices

✅ **DO:**
- Đặt tên test rõ ràng: `methodName_ShouldExpectedBehavior_WhenCondition()`
- Test 1 behavior duy nhất cho mỗi test
- Sử dụng AAA pattern: Arrange → Act → Assert
- Mock external dependencies
- Kiểm tra edge cases & error scenarios

❌ **DON'T:**
- Test implementation details
- Viết tests quá dài (> 20 lines)
- Share state giữa tests
- Mock class đang test
- Bỏ qua error cases


