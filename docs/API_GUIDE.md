# REST & WebSocket API Specification

> Exhaustive API reference for the Real Estate Management Platform. Covers OpenAPI 3.0 specs, JWT Bearer security schemes, payload envelopes, and complete endpoint catalogs across all business domains.

---

## 1. Interactive Explorer & Specifications

The backend serves self-documenting interactive Swagger UI and OpenAPI specifications directly from the running instance:

- **Swagger UI Interactive Console**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3.0 Specification (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **OpenAPI 3.0 Specification (YAML)**: [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)
- **Postman Collection**: [`docs/QL_BDS.postman_collection.json`](QL_BDS.postman_collection.json)

### Authorization Scheme in Swagger UI
1. Execute `POST /api/auth/login` to obtain your `accessToken`.
2. Click the **Authorize** button in the top right corner of Swagger UI.
3. Enter `Bearer <your_token>` and submit.
4. Subsequent API invocations will automatically attach the `Authorization` header.

---

## 2. Standardized Response Envelope & Status Codes

All responses follow a predictable JSON envelope structure:

### Success Response Format
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Error Response Format
```json
{
  "timestamp": "2026-11-06T20:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: price must be greater than 0",
  "path": "/api/properties"
}
```

### HTTP Status Code Matrix

| Status Code | Code Meaning | Usage in Platform |
| :--- | :--- | :--- |
| `200 OK` | Request Succeeded | Returned for successful read/update operations. |
| `201 Created` | Resource Created | Returned upon successful registration, property listing, or appointment creation. |
| `204 No Content` | Deleted / Void | Returned for successful deletion operations. |
| `400 Bad Request` | Validation Failure | Returned when input parameters fail constraint validation or bad data payloads. |
| `401 Unauthorized`| Missing / Invalid Token | Returned when JWT token is expired, tampered, or omitted. |
| `403 Forbidden` | Access Denied | Returned when user role lacks permission (e.g., standard User attempting Admin endpoints). |
| `404 Not Found` | Entity Missing | Returned when requested property, viewing, or user does not exist. |
| `409 Conflict` | Business Collision | Returned on scheduling time-slot collisions or duplicate unique email/username. |
| `429 Too Many Req`| Rate Limited | Returned when exceeding OTP request limits (3 requests per hour). |

---

## 3. Authentication & Security Endpoints (`/api/auth`)

| Method | Endpoint | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register new user account; sends verification OTP to email. |
| `POST` | `/api/auth/verify-otp` | Public | Verify OTP code and activate account; returns initial tokens. |
| `POST` | `/api/auth/resend-otp` | Public | Resend verification OTP code (rate-limited to 3/hr). |
| `POST` | `/api/auth/login` | Public | Authenticate credentials; returns access & refresh tokens. |
| `POST` | `/api/auth/refresh-token` | Public | Obtain new access token using a valid refresh token. |
| `POST` | `/api/auth/forgot-password`| Public | Initiate password recovery; sends reset code to email. |
| `POST` | `/api/auth/reset-password` | Public | Set new password using verified reset code. |
| `POST` | `/api/auth/logout` | Authenticated | Revoke active refresh token and invalidate session. |

### Sample: User Registration
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "investor@example.com",
  "password": "SecurePassword123!",
  "fullName": "Nguyen Van A",
  "phone": "0912345678"
}
```

---

## 4. User Directory & Role Escalation (`/api/users` & `/api/roles`)

| Method | Endpoint | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users/profile` | Authenticated | Retrieve profile details of currently authenticated user. |
| `PUT` | `/api/users/profile` | Authenticated | Update user profile details (fullName, phone, avatar). |
| `PUT` | `/api/users/change-password` | Authenticated | Change account password. |
| `POST` | `/api/roles/upgrade-to-owner` | `ROLE_USER` | Self-upgrade user account to property Owner. |
| `POST` | `/api/roles/request-agent` | `ROLE_USER` | Submit certified Agent application for Admin review. |
| `GET` | `/api/roles/agent-requests` | `ROLE_ADMIN` | List pending agent certification requests. |
| `PUT` | `/api/roles/agent-requests/{id}/approve` | `ROLE_ADMIN` | Approve Realtor application, upgrading role to `ROLE_AGENT`. |
| `PUT` | `/api/roles/agent-requests/{id}/reject` | `ROLE_ADMIN` | Reject Realtor application with explanatory feedback. |
| `GET` | `/api/users` | `ROLE_ADMIN` | List all system accounts with pagination and filtering. |
| `DELETE`| `/api/users/{id}` | `ROLE_ADMIN` | Soft-delete / deactivate user account. |

---

## 5. Property Catalog & Media Management (`/api/properties`)

| Method | Endpoint | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/properties` | Public | Search properties with filters (city, district, price, area, rooms). |
| `GET` | `/api/properties/{id}` | Public | Get property by ID (increments view counter). |
| `GET` | `/api/properties/slug/{slug}`| Public | Get property by SEO slug. |
| `GET` | `/api/properties/{id}/similar`| Public | Get recommended similar properties. |
| `POST` | `/api/properties` | `OWNER`, `AGENT` | Create new property listing in `PENDING` status. |
| `PUT` | `/api/properties/{id}` | `OWNER`, `AGENT` | Update existing property listing. |
| `DELETE`| `/api/properties/{id}` | `OWNER`, `AGENT`, `ADMIN` | Soft-delete property listing. |
| `POST` | `/api/properties/{id}/images` | `OWNER`, `AGENT` | Upload multipart image files directly to Cloudinary CDN. |
| `DELETE`| `/api/properties/{id}/images/{imageId}` | `OWNER`, `AGENT` | Delete image from listing and Cloudinary storage. |
| `PUT` | `/api/properties/{id}/approve` | `ROLE_ADMIN` | Approve property listing for public search catalog. |
| `PUT` | `/api/properties/{id}/reject` | `ROLE_ADMIN` | Reject listing with reason feedback. |
| `PUT` | `/api/properties/{id}/status` | `OWNER`, `AGENT` | Update listing status (`AVAILABLE`, `HIDDEN`, `SOLD`). |

### Sample: Property Search with Query Parameters
```http
GET /api/properties?city=Ho+Chi+Minh&district=District+1&minPrice=2000000000&maxPrice=10000000000&bedrooms=2&page=0&size=10
```

---

## 6. Viewing Appointments Scheduler (`/api/viewings`)

| Method | Endpoint | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/viewings/available-slots` | Public / Auth | Check vacant viewing timeslots for a property on a given date. |
| `POST` | `/api/viewings` | Authenticated | Book a property viewing appointment. |
| `GET` | `/api/viewings/my-bookings` | Authenticated | Get list of viewing appointments booked by current user. |
| `GET` | `/api/viewings/my-properties` | `OWNER`, `AGENT` | Get viewing appointments scheduled for caller's properties. |
| `PUT` | `/api/viewings/{id}/confirm` | `OWNER`, `AGENT` | Confirm pending viewing appointment. |
| `PUT` | `/api/viewings/{id}/reject` | `OWNER`, `AGENT` | Reject viewing appointment with reason. |
| `PUT` | `/api/viewings/{id}/reschedule` | Authenticated | Propose rescheduled date/timeslot. |
| `PUT` | `/api/viewings/{id}/complete`| `OWNER`, `AGENT` | Mark viewing as successfully completed. |
| `PUT` | `/api/viewings/{id}/cancel` | Authenticated | Cancel scheduled viewing appointment. |

---

## 7. Real-Time Chat & Messaging (`/api/conversations` & `/ws`)

### 7.1. REST Endpoints

| Method | Endpoint | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/conversations` | Authenticated | List all active conversations for the authenticated user. |
| `POST` | `/api/conversations/property/{propertyId}` | Authenticated | Initiate or retrieve conversation tied to a property. |
| `GET` | `/api/conversations/{id}/messages` | Authenticated | Fetch paginated chat history for conversation. |
| `PUT` | `/api/conversations/{id}/read` | Authenticated | Mark conversation as read and reset unread counter. |
| `PUT` | `/api/conversations/messages/{id}` | Authenticated | Edit previously sent message content. |
| `DELETE`| `/api/conversations/messages/{id}/recall` | Authenticated | Recall / unsend message. |

### 7.2. WebSocket STOMP Messaging Specification

- **Broker Endpoint**: `/ws`
- **Outgoing Message Destination**: `/app/chat.sendMessage`
  ```json
  {
    "conversationId": 12,
    "content": "Is this apartment available for a weekend visit?"
  }
  ```
- **Incoming Broadcast Destination**: `/topic/messages/{conversationId}`
  ```json
  {
    "id": 481,
    "conversationId": 12,
    "senderId": 5,
    "content": "Is this apartment available for a weekend visit?",
    "isEdited": false,
    "isRecalled": false,
    "createdAt": "2026-11-06T20:30:15Z"
  }
  ```
- **Presence Destination**: `/topic/presence/{userId}`
  ```json
  {
    "userId": 5,
    "status": "ONLINE"
  }
  ```

---

## 8. Wishlist & Favorites (`/api/favorites`)

| Method | Endpoint | Access Level | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/favorites` | Authenticated | Retrieve list of all bookmarked properties for user. |
| `POST` | `/api/favorites/{propertyId}` | Authenticated | Add property to user favorites. |
| `DELETE`| `/api/favorites/{propertyId}` | Authenticated | Remove property from user favorites. |
| `GET` | `/api/favorites/check/{propertyId}` | Authenticated | Check if specific property is bookmarked ($O(1)$ query). |
| `DELETE`| `/api/favorites/clear` | Authenticated | Clear all properties from wishlist. |

