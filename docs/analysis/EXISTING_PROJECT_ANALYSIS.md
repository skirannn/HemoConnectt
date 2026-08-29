# HemoConnect — Existing Project Analysis & Migration Plan

This document is the output of Step 1 you asked for: inspecting the uploaded
`Hemoconnect-application-main.zip`, understanding what it actually does, and
mapping every real feature onto the new Java Full Stack architecture.

---

## 1. What the existing project actually is

The uploaded project is a **MERN application** (not a generic template):

| Layer | Technology found in the zip |
|---|---|
| Frontend | React 18 + React Router v7 + Chakra UI + Vite |
| Backend | Node.js + Express |
| Database | MongoDB (Mongoose ODM), DB name `sai_new` |
| Auth | JWT (`jsonwebtoken`) + `bcryptjs` |
| Real-time | Socket.IO (per-user rooms for notifications) |

Entry point: `server/index.js` wires up 8 route groups, connects to MongoDB,
and starts a Socket.IO server alongside Express.

## 2. Existing pages (React) — `src/pages`

| Folder | Page | Purpose |
|---|---|---|
| Home | HomePage | Landing page |
| Auth | LoginPage, SignupPage, ForgotPasswordPage, ProfileSetupPage, ProfilePage | Auth + profile completion flow |
| Dashboard | DonorDashboard, RecipientDashboard, AdminDashboard | Role-specific dashboards |
| Request | BloodRequestPage | Recipient creates a blood request |
| Matching | DonorMatchingPage | Donor/recipient finds matching donors |
| Notifications | NotificationsPage | List + mark-as-read |
| History | DonorHistoryPage, RecipientHistoryPage | Past donations / past requests |
| Contact | ContactPage | Contact Us form |
| About | AboutPage | Static info |
| Error | NotFoundPage, UnauthorizedPage, ServerErrorPage, NoDonorsPage | Error/edge states |

Routing (`src/App.jsx`) uses a `ProtectedRoute` component that:
1. Redirects to `/login` if not authenticated.
2. Redirects to `/unauthorized` if the route requires a role the user doesn't have.
3. Redirects to `/profile-setup` if `user.profileComplete === false`.

**This "must complete profile before using the app" rule is an important
business rule** and is preserved in the rebuild (see `profileCompleted` flag
on the User entity, Section 6 of your brief).

## 3. Existing backend — models, routes, business rules

### 3.1 User (`server/models/User.js`)
Fields: `name, email(unique), password(hashed), bloodGroup, location, role
[donor|recipient|admin] default donor, profileComplete, isAvailable, phone,
address, emergencyContact, profilePic, medicalConditions, medications,
lastDonationDate, lastActive, hospital, doctorName, doctorContact,
urgencyLevel, createdAt, updatedAt`.

This is one collection used for **all three roles** (donor-only and
recipient-only fields just sit unused depending on role). We preserve this
single-table-per-role-flag approach because it's simple to learn — see
Section 6 mapping below.

### 3.2 Auth (`server/routes/auth.js`, `server/middleware/auth.js`)
- `POST /api/auth/signup` — checks duplicate email, hashes password with
  bcrypt (cost 12), creates user, signs JWT `{userId, email}`.
- `POST /api/auth/login` — verifies password, signs JWT (24h, or 7d if
  `rememberMe`), updates `lastLogin`.
- `GET /api/auth/verify` — validates token, returns current user (used by
  `AuthContext` on page load to restore session).
- `POST /api/auth/forgot-password`, `/send-otp`, `/reset-password` — an
  **in-memory OTP store** (a plain JS object) is used for password reset.
  This is a demo-only mechanism (OTP is even returned in the response for
  testing) and does not survive a server restart.
- JWT secret defaults to `'your-secret-key'` if `JWT_SECRET` is not set —
  **a real security gap** we deliberately fix (env-var required, no default)
  in the rebuild.
- Role check middleware: `requireRole(role)` / `requireRoles([roles])`.

### 3.3 Blood Request (`server/models/BloodRequest.js`,
`server/routes/blood-requests.js`)
This is the richest model in the project. Core fields: requester info,
`bloodGroup`, `units`, `urgencyLevel [low|medium|high|critical]`, hospital
info, `location`, `requiredByDate`, `status
[pending|matched|confirmed|in_progress|fulfilled|cancelled|expired]`, an
embedded array of donor `responses` (accept/decline/maybe), an embedded
array of `matchedDonors`, and admin fields (`isVerified`, `isFlagged`, ...).

Business rules found in the model itself:
- A request auto-expires 30 days after creation (`expiresAt`), and Mongo's
  TTL index actually deletes it.
- `addResponse()` — appending a response with type `accept` automatically
  flips `status` to `matched`.
- `confirmDonor()` — moves status to `confirmed` and stamps the matched
  donor's sub-document.
- Routes: create, list *active* requests (public ticker), list *nearby*
  requests for a donor (same blood group + location text match), list "my
  requests", respond to a request, confirm a donor, cancel, mark fulfilled.

### 3.4 Donor blood profile (`server/models/DonorBlood.js`,
`server/routes/donor-blood.js`)
A **separate, much heavier collection** from `User`, one-to-one with a
donor. Holds `rhFactor`, full `medicalHistory` (diabetes, HIV, pregnancy,
medications, allergies, ...), `physicalInfo` (age/weight/height/gender),
`availability` (preferred locations, time slots, max distance, emergency
only), an embedded `donationHistory` array, and rolling `stats`
(acceptedRequests, responseRate, etc.).

Key business rule (in a Mongoose pre-save hook):
> **A donor becomes eligible again 56 days after their last donation.**
> `nextEligibleDate = lastDonationDate + 56 days`; `isEligible` is
> recalculated from that on every save.

Matching queries: `findEligibleDonors(bloodGroup, location)` — eligible +
available + active + (no pending cooldown), `findEmergencyDonors`.

This is genuinely the most valuable domain logic in the whole project — the
56-day eligibility rule and the matching filters are what we carry into the
Java `DonorMatchingService`.

### 3.5 Notification (`server/models/Notification.js`,
`server/routes/notifications.js`)
Simple: `userId, type, title, message, isRead, timestamp`. CRUD is minimal —
create, list-for-user, mark-as-read. Socket.IO also pushes a live event to
`user_${userId}` room (e.g. when an admin rejects a request). We keep the
REST CRUD; Section 11 of your brief explicitly says WebSockets can come
later, so Module 6 ships REST-only first.

### 3.6 Admin (`server/routes/admin.js`)
`/stats` (totalUsers, activeRequests, flagged count — note
`completedDonations` is **hardcoded mock data**, not real, in the original),
`/recent-users`, `/flagged-requests`, approve/reject a flagged request
(reject also fires a notification).

### 3.7 Contact (`server/models/ContactMessage.js`,
`server/routes/contact.js`)
Public POST to submit a message (name/email/phone/subject/message/category/
priority), admin GET list with filters + pagination, GET by id, PATCH status
(with a "mark as responded" sub-flow). Note there are **two** contact models
in the repo (`Contact.js` — a bare unused stub, and `ContactMessage.js` —
the one actually wired into routes). We only migrate `ContactMessage`.

### 3.8 Not migrated — infra/dead code
- `server/routes/requests.js` — **empty file**, unused, not imported by
  `index.js`. Dropped.
- `server/routes/collections.js` and `server/routes/dynamic-models.js` +
  `server/utils/modelFactory.js` / `collectionManager.js` — a generic
  "create arbitrary MongoDB collections/schemas at runtime" admin toy. It's
  infrastructure for schemaless experimentation, not a HemoConnect feature,
  and it's meaningless once we move to a fixed relational schema. Dropped,
  with this note so nothing is silently lost.
- `Contact.js` (the unused duplicate model). Dropped.

---

## 4. Functionality → New Java Full Stack Mapping

| Existing (MERN) | New (Java Full Stack) | Module |
|---|---|---|
| `models/User.js` + role string | `entity.User` + `enum Role {DONOR, RECIPIENT, ADMIN}` + `enum BloodGroup` | 1 |
| `routes/auth.js` (signup/login/verify, bcrypt, JWT) | `AuthController` + Spring Security + `JwtService` (env-based secret, no hardcoded fallback) | 2 |
| OTP-in-memory-object password reset | Kept conceptually (documented as a *demo* mechanism) but reimplemented with a proper `PasswordResetToken` table instead of an in-memory map, so it survives a restart | 2 |
| `models/DonorBlood.js` + `routes/donor-blood.js` | `entity.DonorProfile` (1:1 with User) + `DonorProfileService`, keeping the 56-day eligibility rule and availability/location fields | 3 |
| `models/BloodRequest.js` (incl. embedded `responses[]`) | `entity.BloodRequest` + `entity.DonorResponse` (`@OneToMany`) with the same status lifecycle | 4 |
| `DonorBlood.statics.findEligibleDonors` | `DonorMatchingService.findMatches(bloodRequest)` — plain JPQL/Spring Data query, same 3 filters (blood group, availability, location) | 5 |
| `models/Notification.js` + routes | `entity.Notification` + REST CRUD (WebSocket/Socket.IO push deferred, as you specified) | 6 |
| `routes/admin.js` (stats, flagged requests, approve/reject) | `AdminController` — stats computed from real queries (the mock `completedDonations: 45` is replaced with a real `COUNT` query, not left fake) | 7 |
| `models/ContactMessage.js` + `routes/contact.js` | `entity.ContactMessage` + `ContactController` | 8 |
| Chakra UI React pages, `AuthContext`, `ProtectedRoute` | Same React pages/structure, calling the new Spring Boot REST API instead of Express; `ProtectedRoute`'s "redirect to profile-setup if incomplete" rule is preserved | 9 |
| — (none yet) | New `AiController` calling the Anthropic API from the backend only | 10 |

Nothing important is dropped without a stated reason (see Section 3.8).

## 5. Proposed relational schema (replacing MongoDB)

```
users (id PK, name, email UNIQUE, password, phone, blood_group, location,
       role, available_for_donation, profile_completed, created_at, updated_at)

donor_profiles (id PK, user_id FK -> users.id UNIQUE,
                 rh_factor, is_eligible, last_donation_date, next_eligible_date,
                 total_donations, total_units_donated,
                 age, weight, height, gender,
                 max_distance_km, emergency_only, created_at, updated_at)

blood_requests (id PK, requester_id FK -> users.id,
                 blood_group, units_required, urgency, hospital, location,
                 required_date, description, status, created_at, updated_at)

donor_responses (id PK, blood_request_id FK -> blood_requests.id,
                  donor_id FK -> users.id, response_type, response_message,
                  created_at)

notifications (id PK, user_id FK -> users.id, type, title, message,
                is_read, created_at)

contact_messages (id PK, name, email, phone, subject, message, category,
                   priority, status, created_at)
```

`@OneToOne User↔DonorProfile`, `@OneToMany BloodRequest→DonorResponse`,
`@ManyToOne DonorResponse→User(donor)`, `@ManyToOne Notification→User`.
Full DDL will live in `database/schema.sql`, generated as each module is
built (Module 1 ships the `users` table now).

## 6. Final project structure

```
HemoConnect/
├── frontend/                  (Module 9 — React, reused from existing UI)
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/hemoconnect/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       ├── dto/
│       ├── security/
│       ├── exception/
│       └── config/
├── database/
│   └── schema.sql
├── docs/
│   ├── analysis/EXISTING_PROJECT_ANALYSIS.md   (this file)
│   └── modules/user.md, auth.md, ...           (one per module, as built)
├── .gitignore
└── README.md
```

## 7. What's actually implemented right now

Only **Module 1 (User Management)** is implemented in this pass — entity,
repository, service, controller, DTOs, `schema.sql` for `users`, and
`docs/modules/user.md`. Everything else in the table above is a plan, not
code yet. Modules 2–10 will be added the same way, one at a time, exactly as
you asked in Section 26 of your brief.
