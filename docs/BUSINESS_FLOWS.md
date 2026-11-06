# Platform Business Flows & Operational Workflows

> Detailed walkthrough of core operational workflows and user journeys across the Real Estate Management Platform. Includes sequence diagrams, status transition matrices, and transactional validation rules.

---

## 1. Journey 1: Buyer Property Discovery & Wishlist Bookmarking

A prospective buyer or renter searches the public catalog, filters by granular specifications, examines listing details, and saves high-interest properties:

```mermaid
sequenceDiagram
    autonumber
    actor Buyer as Prospective Buyer
    participant API as PropertyController
    participant Svc as PropertyService
    participant Fav as FavoriteService
    participant DB as PostgreSQL

    Buyer->>API: GET /api/properties?city=HCM&district=Q1&minPrice=2B&maxPrice=10B&bedrooms=2
    API->>Svc: searchProperties(filters, pageable)
    Svc->>DB: Query indexed properties WHERE status = 'AVAILABLE'
    DB-->>Svc: Page of property summary entities + primary photos
    Svc-->>API: 200 OK (Paginated listing cards)
    API-->>Buyer: Display matching properties

    Buyer->>API: GET /api/properties/can-ho-cao-cap-quan-1-sg
    API->>Svc: getPropertyBySlug("can-ho-cao-cap-quan-1-sg")
    Svc->>DB: Fetch property details, amenities & all Cloudinary image URLs
    Svc->>DB: UPDATE properties SET view_count = view_count + 1
    Svc-->>API: 200 OK (Full property details)
    API-->>Buyer: Render listing gallery & owner/agent details

    Buyer->>API: POST /api/favorites/142 (Bearer Token)
    API->>Fav: addFavorite(userId, propertyId)
    Fav->>DB: Check if duplicate bookmark exists
    Fav->>DB: INSERT INTO favorites (user_id, property_id)
    Fav->>DB: UPDATE properties SET favorite_count = favorite_count + 1
    Fav-->>API: 201 Created
    API-->>Buyer: Property marked in personal wishlist
```

---

## 2. Journey 2: Viewing Appointment Booking & Conflict Management

The appointment engine ensures no two buyers can book conflicting timeslots for the same property:

```mermaid
sequenceDiagram
    autonumber
    actor Buyer as Buyer
    actor Owner as Owner / Agent
    participant API as ViewingController
    participant Svc as ViewingService
    participant DB as PostgreSQL

    Buyer->>API: GET /api/viewings/available-slots?propertyId=142&date=2026-11-15
    API->>Svc: getAvailableSlots(142, "2026-11-15")
    Svc->>DB: Fetch existing viewings for property on 2026-11-15
    Svc-->>API: List of open 30-minute intervals (e.g. 09:00-09:30, 10:00-10:30)
    API-->>Buyer: Display available calendar slots

    Buyer->>API: POST /api/viewings (propertyId: 142, date: 2026-11-15, time: 10:00-10:30)
    API->>Svc: createViewingBooking(userId, request)
    Svc->>DB: Atomic check: is slot already taken or pending?
    alt Slot is Available
        Svc->>DB: INSERT INTO viewings (status = 'PENDING')
        Svc-->>API: 201 Created (Viewing scheduled, pending confirmation)
        API-->>Buyer: Booking confirmation pending host review
    else Slot Conflict
        Svc-->>API: 409 Conflict (Timeslot has already been reserved)
        API-->>Buyer: Error notification to pick another slot
    end

    Owner->>API: PUT /api/viewings/58/confirm (Bearer Token)
    API->>Svc: confirmViewing(ownerId, viewingId: 58)
    Svc->>DB: UPDATE viewings SET status = 'CONFIRMED'
    Svc-->>API: 200 OK
    API-->>Owner: Appointment confirmed
```

---

## 3. Journey 3: Seller Listing Publication & Cloudinary Media Ingestion

Property owners and certified realtors upload new properties with photos and await administrative quality moderation:

```mermaid
sequenceDiagram
    autonumber
    actor Seller as Owner / Agent
    actor Admin as Platform Admin
    participant API as PropertyController
    participant Svc as PropertyService
    participant CDN as Cloudinary SDK
    participant DB as PostgreSQL

    Seller->>API: POST /api/properties (title, price, area, address, rooms)
    API->>Svc: createProperty(sellerId, request)
    Svc->>Svc: Generate unique SEO slug (e.g. villa-thao-dien-district-2-a9b)
    Svc->>DB: INSERT INTO properties (status = 'PENDING')
    Svc-->>API: 201 Created (propertyId: 185)
    API-->>Seller: Property created, ready for photos

    Seller->>API: POST /api/properties/185/images (Multipart: [photo1.jpg, photo2.jpg])
    API->>Svc: uploadImages(185, files)
    loop Each Image File
        Svc->>CDN: upload(file.getBytes(), options)
        CDN-->>Svc: secure_url, public_id, dimensions
        Svc->>DB: INSERT INTO property_images (property_id, image_url, public_id)
    end
    Svc-->>API: 200 OK (Image URLs attached)
    API-->>Seller: Photos uploaded successfully

    Admin->>API: PUT /api/properties/185/approve (Bearer Token)
    API->>Svc: approveProperty(adminId, 185)
    Svc->>DB: UPDATE properties SET status = 'AVAILABLE'
    Svc-->>API: 200 OK
    API-->>Admin: Property published to active search index
```

---

## 4. Journey 4: Direct Real-Time Inquiry via WebSocket STOMP

Buyers and property managers communicate in real time regarding listing queries:

```mermaid
sequenceDiagram
    autonumber
    actor Buyer as Buyer Client
    actor Agent as Agent Client
    participant STOMP as STOMP Broker (/ws)
    participant ChatCtrl as ConversationController
    participant Svc as MessageService
    participant DB as PostgreSQL

    Buyer->>ChatCtrl: POST /api/conversations/property/185
    ChatCtrl->>DB: Find or create conversation for (property: 185, buyer, agent)
    DB-->>ChatCtrl: conversationId: 64
    ChatCtrl-->>Buyer: 200 OK (conversation details)

    Buyer->>STOMP: CONNECT /ws & SUBSCRIBE /topic/messages/64
    Agent->>STOMP: CONNECT /ws & SUBSCRIBE /topic/messages/64

    Buyer->>STOMP: SEND /app/chat.sendMessage {"conversationId": 64, "content": "Is price negotiable?"}
    STOMP->>Svc: handleIncomingMessage(payload)
    Svc->>DB: INSERT INTO messages (conversation_id, sender_id, content)
    Svc->>DB: UPDATE conversations SET unread_count_agent = unread_count_agent + 1, last_message_at = NOW()
    Svc->>STOMP: Broadcast to /topic/messages/64
    STOMP-->>Agent: Inbound Message Envelope (Instant render)

    Agent->>ChatCtrl: PUT /api/conversations/64/read
    ChatCtrl->>DB: Reset unread_count_agent to 0
    ChatCtrl-->>Agent: 200 OK (Badges cleared)
```

---

## 5. Journey 5: Role Escalation & Certification Workflow

The platform maintains distinct permissions between standard consumers and commercial actors:

```mermaid
sequenceDiagram
    autonumber
    actor User as Registered User
    actor Admin as Platform Admin
    participant API as RoleController
    participant Svc as RoleService
    participant DB as PostgreSQL

    rect rgb(240, 248, 255)
    note right of User: Path A: Instant Owner Upgrade
    User->>API: POST /api/roles/upgrade-to-owner {"idCardNumber": "079123456789"}
    API->>Svc: upgradeToOwner(userId, idCardNumber)
    Svc->>DB: INSERT INTO owners (user_id, id_card_number, verified_at = NOW())
    Svc->>DB: UPDATE users SET role = 'OWNER'
    Svc-->>API: 200 OK (User promoted to OWNER)
    API-->>User: Owner capabilities unlocked (Listing creation enabled)
    end

    rect rgb(255, 250, 240)
    note right of User: Path B: Formal Agent Certification Review
    User->>API: POST /api/roles/request-agent {"licenseNumber": "RE-88992", "agencyName": "Saigon Realty"}
    API->>Svc: submitAgentRequest(userId, payload)
    Svc->>DB: INSERT INTO agent_requests (user_id, license_number, agency_name, status = 'PENDING')
    Svc-->>API: 201 Created (Application under review)
    API-->>User: Verification application dispatched to compliance team

    Admin->>API: PUT /api/roles/agent-requests/12/approve
    API->>Svc: approveAgentRequest(adminId, requestId: 12)
    Svc->>DB: UPDATE agent_requests SET status = 'APPROVED'
    Svc->>DB: INSERT INTO agents (user_id, license_number, agency_name)
    Svc->>DB: UPDATE users SET role = 'AGENT'
    Svc-->>API: 200 OK
    API-->>Admin: Agent credentials certified
    end
```

