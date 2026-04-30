-- =====================================================
-- BDS PLATFORM SCHEMA
-- Target: Spring Boot 4 + PostgreSQL + Flyway
-- =====================================================

SET search_path TO public;

-- =========================
-- ENUM TYPES
-- =========================
CREATE TYPE user_role AS ENUM ('USER', 'OWNER', 'AGENT', 'ADMIN');

CREATE TYPE property_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'HIDDEN');

CREATE TYPE viewing_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED');

-- =========================
-- 1) USERS
-- =========================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    phone VARCHAR(20),

    role user_role NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 2) OWNERS
-- =========================
CREATE TABLE owners (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE,

    address VARCHAR(255),
    description TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_owner_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- =========================
-- 3) AGENTS
-- =========================
CREATE TABLE agents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE,

    agency_name VARCHAR(150),
    license_number VARCHAR(100),
    rating DOUBLE PRECISION DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_agent_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- =========================
-- 4) PROPERTIES
-- =========================
CREATE TABLE properties (
    id BIGSERIAL PRIMARY KEY,

    title VARCHAR(255) NOT NULL,
    description TEXT,

    price NUMERIC(15,2) NOT NULL,
    area DOUBLE PRECISION,
    bedrooms INT,
    bathrooms INT,

    address VARCHAR(255),
    city VARCHAR(100),
    district VARCHAR(100),

    status property_status NOT NULL DEFAULT 'PENDING',
    visibility BOOLEAN DEFAULT TRUE,

    view_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,

    owner_id BIGINT,
    agent_id BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_property_owner
        FOREIGN KEY (owner_id) REFERENCES owners(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_property_agent
        FOREIGN KEY (agent_id) REFERENCES agents(id)
        ON DELETE SET NULL
);

-- =========================
-- 5) PROPERTY IMAGES
-- =========================
CREATE TABLE property_images (
    id BIGSERIAL PRIMARY KEY,

    property_id BIGINT NOT NULL,
    image_url TEXT NOT NULL,
    is_main BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_image_property
        FOREIGN KEY (property_id) REFERENCES properties(id)
        ON DELETE CASCADE
);

-- =========================
-- 6) VIEWINGS
-- =========================
CREATE TABLE viewings (
    id BIGSERIAL PRIMARY KEY,

    property_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    scheduled_time TIMESTAMP NOT NULL,

    status viewing_status DEFAULT 'PENDING',
    note TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_viewing_property
        FOREIGN KEY (property_id) REFERENCES properties(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_viewing_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- =========================
-- 7) FAVORITES
-- =========================
CREATE TABLE favorites (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_favorite UNIQUE (user_id, property_id),

    CONSTRAINT fk_fav_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_fav_property
        FOREIGN KEY (property_id) REFERENCES properties(id)
        ON DELETE CASCADE
);

-- =========================
-- 8) CONVERSATIONS
-- =========================
CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,

    property_id BIGINT,
    user_one_id BIGINT NOT NULL,
    user_two_id BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_conversation UNIQUE (property_id, user_one_id, user_two_id),

    CONSTRAINT fk_conv_property
        FOREIGN KEY (property_id) REFERENCES properties(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_conv_user_one
        FOREIGN KEY (user_one_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_conv_user_two
        FOREIGN KEY (user_two_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- =========================
-- 9) MESSAGES
-- =========================
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,

    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,

    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- =========================
-- INDEXES
-- =========================

-- Properties search
CREATE INDEX idx_properties_city ON properties(city);
CREATE INDEX idx_properties_district ON properties(district);
CREATE INDEX idx_properties_price ON properties(price);
CREATE INDEX idx_properties_status ON properties(status);
CREATE INDEX idx_properties_owner ON properties(owner_id);
CREATE INDEX idx_properties_agent ON properties(agent_id);

-- Images
CREATE INDEX idx_images_property ON property_images(property_id);

-- Viewings
CREATE INDEX idx_viewings_property ON viewings(property_id);
CREATE INDEX idx_viewings_user ON viewings(user_id);
CREATE INDEX idx_viewings_time ON viewings(scheduled_time);
CREATE INDEX idx_viewing_property_time ON viewings(property_id, scheduled_time);

-- Favorites
CREATE INDEX idx_favorites_user ON favorites(user_id);

-- Messages
CREATE INDEX idx_messages_conv ON messages(conversation_id);
CREATE INDEX idx_messages_time ON messages(created_at);
