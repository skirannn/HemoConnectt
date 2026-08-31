# HemoConnect (Java Full Stack rebuild)

HemoConnect is a blood donation coordination platform: recipients raise
blood requests, donors get matched and respond, and admins moderate the
whole system.

This repository is a **from-scratch Java Full Stack rebuild** of an
existing HemoConnect MERN project (Node/Express/MongoDB + React), built to
learn Spring Boot properly while preserving the real business rules of the
original app. See `docs/analysis/EXISTING_PROJECT_ANALYSIS.md` for the full
breakdown of what was in the original project and how it maps here.

> **Status:** Modules 1–8 (User Management through Contact) and Module 10
> (Generative AI) are fully implemented on the backend. Module 9 (React ↔
> Spring Boot integration) is partially done - the authentication flow
> (signup, login, session restore, logout, forgot-password) is fully
> wired end to end against the real backend; the remaining pages are
> documented but not yet migrated (see
> `docs/modules/frontend-integration.md`). This README is updated
> honestly as each piece ships - nothing below is claimed as "done"
> unless it's actually in the code.

## Technology stack

**Frontend:** React.js, JavaScript, HTML, CSS
**Backend:** Java 17, Spring Boot 3, Spring Web, Spring Data JPA
(Hibernate), Spring Security, JWT, Jakarta Bean Validation, Maven
**Database:** MySQL
**AI (later):** A backend-only Generative AI assistant module

## Architecture

```
React.js  →  REST API  →  Controller  →  Service  →  Repository  →  JPA/Hibernate  →  MySQL
```

Backend package layout:

```
com.hemoconnect
├── controller   (REST endpoints)
├── service      (business logic)
├── repository   (database access - Spring Data JPA interfaces)
├── entity       (JPA entities, i.e. database tables)
├── dto          (request/response shapes sent over HTTP)
├── security     (JWT filter, Spring Security config - Module 2+)
├── exception    (custom exceptions + @RestControllerAdvice)
└── config       (Spring @Configuration classes)
```

## Modules

| # | Module | Status |
|---|---|---|
| 1 | User Management | ✅ Implemented |
| 2 | Authentication + JWT + Spring Security | ✅ Implemented |
| 3 | Donor | ✅ Implemented |
| 4 | Blood Request | ✅ Implemented |
| 5 | Donor Matching | ✅ Implemented |
| 6 | Notifications | ✅ Implemented |
| 7 | Admin | ✅ Implemented |
| 8 | Contact | ✅ Implemented |
| 9 | React ↔ Spring Boot integration | 🟡 Partial (auth flow fully wired; other pages documented, not yet migrated) |
| 10 | Generative AI assistant | ✅ Implemented (backend) |

Each module gets its own explanation doc under `docs/modules/`, written for
learning (see `docs/modules/user.md` for Module 1).

## Database

See `database/schema.sql` for the schema built so far, and Section 5 of
`docs/analysis/EXISTING_PROJECT_ANALYSIS.md` for the full target design
(entities, relationships, and why each one exists).

## API overview (so far)

```
POST   /api/auth/signup           - register, returns { token, user }
POST   /api/auth/login            - log in, returns { token, user }
GET    /api/auth/verify           - returns the current user (requires a token)
POST   /api/auth/forgot-password  - confirms an account exists
POST   /api/auth/send-otp         - generates a 6-digit OTP (logged server-side)
POST   /api/auth/reset-password   - verifies OTP + sets a new password

GET    /api/users/{id}            - get one user's public profile   [requires login]
GET    /api/users                 - list all users                  [requires login]
PUT    /api/users/{id}/profile    - update profile fields            [requires login]
DELETE /api/users/{id}            - delete a user                    [requires login]

PUT    /api/donors/{userId}/profile      - create/update donor profile  [self or ADMIN]
GET    /api/donors/{userId}/profile      - view donor profile           [self or ADMIN]
POST   /api/donors/{userId}/donations    - record a completed donation  [self or ADMIN]

POST   /api/blood-requests               - create a request        [RECIPIENT]
GET    /api/blood-requests/{id}          - view one request         [any logged-in user]
GET    /api/blood-requests/active        - list open requests       [any logged-in user]
GET    /api/blood-requests/mine          - requester's own history  [RECIPIENT]
POST   /api/blood-requests/{id}/respond  - accept/decline/maybe     [DONOR]
POST   /api/blood-requests/{id}/confirm  - confirm one donor        [requester or ADMIN]
POST   /api/blood-requests/{id}/fulfill  - mark donation completed  [requester or ADMIN]
POST   /api/blood-requests/{id}/cancel   - cancel the request       [requester or ADMIN]
GET    /api/blood-requests/{id}/matches  - candidate donors for this request  [requester or ADMIN]

GET    /api/notifications                - my notifications              [requires login]
GET    /api/notifications/unread-count   - unread count for a badge      [requires login]
PATCH  /api/notifications/{id}/read      - mark one notification as read [requires login, must own it]
PATCH  /api/notifications/read-all       - mark every notification read  [requires login]

GET    /api/admin/stats                          - dashboard overview counts       [ADMIN]
GET    /api/admin/users                           - every user                      [ADMIN]
DELETE /api/admin/users/{id}                      - delete a user                   [ADMIN]
GET    /api/admin/donors                          - every donor profile             [ADMIN]
GET    /api/admin/blood-requests                  - every request, any status       [ADMIN]
GET    /api/admin/blood-requests/flagged          - requests awaiting review        [ADMIN]
POST   /api/admin/blood-requests/{id}/flag        - flag a request                  [ADMIN]
POST   /api/admin/blood-requests/{id}/approve     - clear a flag                    [ADMIN]
POST   /api/admin/blood-requests/{id}/reject      - cancel a flagged request         [ADMIN]

POST   /api/contact                       - submit a message               [public, no login]
GET    /api/contact?status=NEW            - list messages (filter optional) [ADMIN]
GET    /api/contact/{id}                  - view one message                [ADMIN]
PATCH  /api/contact/{id}/status           - update a message's status       [ADMIN]

POST   /api/ai/chat                       - ask the HemoConnect Assistant   [requires login]
```

More endpoints (`/api/donors/**`, `/api/blood-requests/**`,
`/api/notifications/**`, `/api/admin/**`, `/api/contact/**`, `/api/ai/**`)
will be documented here as their modules are implemented.

## Authentication

Implemented as of Module 2: JWT-based, stateless. Every endpoint except
`/api/auth/**` and `/api/health` now requires a valid
`Authorization: Bearer <token>` header — see
`backend/.../config/SecurityConfig.java`. Role-specific rules (donor-only,
admin-only) are layered on top in later modules. See
`docs/modules/auth.md` for the full explanation.

## Setup instructions

### Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8+ running locally (or update `DB_URL` to point elsewhere)
- Node.js 18+ (only needed once the frontend module lands)

### Environment variables

Copy `.env.example` to `.env` and fill in real values, or export the same
variables in your shell. See that file for the full list.

### How to run the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. On first run, Hibernate creates
the `hemoconnect` database and `users` table automatically
(`ddl-auto=update`), matching `database/schema.sql`.

### How to run the tests

```bash
cd backend
mvn test
```

### How to run the frontend

```bash
cd frontend
npm install
npm run dev
```

Starts on `http://localhost:5173`. The dev server proxies `/api/**`
straight to the backend on `http://localhost:8080` (see
`vite.config.js`), so start the backend first. Signup, login, session
restore, logout, and forgot-password all work end to end against the
real Spring Boot API. See `docs/modules/frontend-integration.md` for
exactly what's wired up and how to migrate the remaining pages yourself.

## Screenshots

_Added once more of the frontend is migrated (Module 9)._

## Future enhancements

- Real-time notifications via WebSockets (the original project used
  Socket.IO for this; deferred per the project brief).
- Generative AI assistant (Module 10) for onboarding/navigation help —
  explicitly not a medical diagnosis tool.
