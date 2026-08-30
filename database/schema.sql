-- HemoConnect database schema
-- This file is documentation of the schema Hibernate generates automatically
-- (spring.jpa.hibernate.ddl-auto=update). You do NOT need to run this by
-- hand for the app to work locally - it's here so you can read the schema
-- without starting the app, and so you have something to run against a
-- fresh MySQL instance if you ever turn ddl-auto off.
--
-- Grows module by module. Only the `users` table exists so far (Module 1).

CREATE DATABASE IF NOT EXISTS hemoconnect;
USE hemoconnect;

-- ============================================================
-- MODULE 1: users
-- One row per account, regardless of role (donor/recipient/admin).
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    email                   VARCHAR(255) NOT NULL UNIQUE,
    password                VARCHAR(255) NOT NULL,   -- BCrypt hash, never plain text
    phone                   VARCHAR(20),
    blood_group             VARCHAR(20)  NOT NULL,   -- enum name, e.g. 'O_POSITIVE'
    location                VARCHAR(255) NOT NULL,
    role                    VARCHAR(20)  NOT NULL,   -- 'DONOR' | 'RECIPIENT' | 'ADMIN'
    available_for_donation  BOOLEAN      NOT NULL DEFAULT TRUE,
    profile_completed       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at              DATETIME     NOT NULL,
    updated_at              DATETIME     NOT NULL
);

-- ============================================================
-- MODULE 2: password_reset_otps
-- Replaces the original project's in-memory OTP object with a real,
-- persisted table (see docs/modules/auth.md for why).
-- ============================================================
CREATE TABLE IF NOT EXISTS password_reset_otps (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(255) NOT NULL,
    otp_code    VARCHAR(10)  NOT NULL,
    expires_at  DATETIME     NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME     NOT NULL
);

-- ============================================================
-- MODULE 3: donor_profiles
-- One row per DONOR user, holding donor-only medical/eligibility data.
-- ============================================================
CREATE TABLE IF NOT EXISTS donor_profiles (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id               BIGINT NOT NULL UNIQUE,
    last_donation_date    DATE,
    next_eligible_date    DATE,
    is_eligible           BOOLEAN NOT NULL DEFAULT TRUE,
    total_donations       INT NOT NULL DEFAULT 0,
    total_units_donated   INT NOT NULL DEFAULT 0,
    age                   INT,
    weight                DOUBLE,
    height                DOUBLE,
    gender                VARCHAR(10),
    max_distance_km       INT,
    emergency_only        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at            DATETIME NOT NULL,
    updated_at            DATETIME NOT NULL,
    CONSTRAINT fk_donor_profile_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ============================================================
-- MODULE 4: blood_requests + donor_responses
-- ============================================================
CREATE TABLE IF NOT EXISTS blood_requests (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id        BIGINT NOT NULL,
    blood_group         VARCHAR(20) NOT NULL,
    units_required      INT NOT NULL,
    hospital            VARCHAR(255),
    location            VARCHAR(255) NOT NULL,
    urgency             VARCHAR(10) NOT NULL,   -- LOW | MEDIUM | HIGH | CRITICAL
    required_date       DATE,
    description         VARCHAR(1000),
    status              VARCHAR(15) NOT NULL DEFAULT 'PENDING',
                        -- PENDING | MATCHED | CONFIRMED | FULFILLED | CANCELLED | EXPIRED
    confirmed_donor_id  BIGINT,
    expires_at          DATETIME NOT NULL,      -- created_at + 30 days
    created_at          DATETIME NOT NULL,
    updated_at          DATETIME NOT NULL,
    CONSTRAINT fk_blood_request_requester FOREIGN KEY (requester_id) REFERENCES users(id),
    CONSTRAINT fk_blood_request_confirmed_donor FOREIGN KEY (confirmed_donor_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS donor_responses (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    blood_request_id    BIGINT NOT NULL,
    donor_id            BIGINT NOT NULL,
    response_type       VARCHAR(10) NOT NULL,   -- ACCEPT | DECLINE | MAYBE
    response_message    VARCHAR(500),
    created_at          DATETIME NOT NULL,
    CONSTRAINT fk_donor_response_request FOREIGN KEY (blood_request_id) REFERENCES blood_requests(id),
    CONSTRAINT fk_donor_response_donor FOREIGN KEY (donor_id) REFERENCES users(id)
);

-- ============================================================
-- MODULE 6: notifications
-- ============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    type        VARCHAR(25) NOT NULL,   -- NEW_MATCHING_REQUEST | DONOR_RESPONSE | STATUS_CHANGE
    title       VARCHAR(255) NOT NULL,
    message     VARCHAR(1000) NOT NULL,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  DATETIME NOT NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ------------------------------------------------------------
-- Tables below are PLANNED (per docs/analysis/EXISTING_PROJECT_ANALYSIS.md)
-- and will be added to this file as each module is implemented.
-- Listed here so the full target schema is visible up front.
-- ------------------------------------------------------------

-- (MODULE 5: Donor Matching added no new table - it's a query layer on
--  top of donor_profiles + blood_requests. See docs/modules/donor-matching.md)

-- MODULE 8: contact_messages (standalone)
