# HemoConnect (Java Full Stack rebuild)

HemoConnect is a blood donation coordination platform: recipients raise
blood requests, donors get matched and respond, and admins moderate the
whole system.

This repository is a **from-scratch Java Full Stack rebuild** of an
existing HemoConnect MERN project (Node/Express/MongoDB + React), built to
learn Spring Boot properly while preserving the real business rules of the
original app. See `docs/analysis/EXISTING_PROJECT_ANALYSIS.md` for the full
breakdown of what was in the original project and how it maps here.

> **Status:** Modules 1 (User Management) and 2 (Authentication + JWT +
> Spring Security) are implemented and working. Modules 3–10 are designed
> (see the analysis doc) but not yet built — this README will be updated
> honestly as each one ships. Nothing below is claimed as "done" unless
> it's actually in the code.

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
| 3 | Donor | ⏳ Planned |
| 4 | Blood Request | ⏳ Planned |
| 5 | Donor Matching | ⏳ Planned |
| 6 | Notifications | ⏳ Planned |
| 7 | Admin | ⏳ Planned |
| 8 | Contact | ⏳ Planned |
| 9 | React ↔ Spring Boot integration | ⏳ Planned |
| 10 | Generative AI assistant | ⏳ Planned |

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

Not yet available — the `frontend/` folder is created in Module 9, when
the existing React UI is wired up to this backend.

## Screenshots

_Added once the frontend module is connected._

## Future enhancements

- Real-time notifications via WebSockets (the original project used
  Socket.IO for this; deferred per the project brief).
- Generative AI assistant (Module 10) for onboarding/navigation help —
  explicitly not a medical diagnosis tool.
