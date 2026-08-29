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

-- ------------------------------------------------------------
-- Tables below are PLANNED (per docs/analysis/EXISTING_PROJECT_ANALYSIS.md)
-- and will be added to this file as each module is implemented.
-- Listed here so the full target schema is visible up front.
-- ------------------------------------------------------------

-- MODULE 3: donor_profiles (1:1 with users, donor-only medical/eligibility data)
-- MODULE 4: blood_requests (many:1 with users as requester)
-- MODULE 4: donor_responses (many:1 with blood_requests and users as donor)
-- MODULE 6: notifications (many:1 with users)
-- MODULE 8: contact_messages (standalone)
