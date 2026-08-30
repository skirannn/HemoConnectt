# Module 8: Contact

## 1. What problem does the Contact module solve?

Not everyone reaching out needs (or has) an account - a donor locked out
of their login, a journalist, someone reporting a bug. This module is the
one place in the whole app where a real person can reach the team without
signing up first.

## 2. What is the Entity?

`ContactMessage` is deliberately **not** linked to `User` with a foreign
key. Every other entity in this app (`DonorProfile`, `BloodRequest`,
`Notification`) belongs to a logged-in user. A Contact Us submission is
the opposite case on purpose - we just store whatever name/email the
person typed in, because requiring an account would defeat the point of a
"contact us if something's wrong" form.

**A cleanup worth naming**: the original project actually had two
contact-related Mongoose models - `Contact.js`, which was never wired
into any route (dead code), and `ContactMessage.js`, the one the real
`/api/contact` routes actually used. We only migrated the second one; see
`docs/analysis/EXISTING_PROJECT_ANALYSIS.md` for the full list of things
intentionally left behind.

## 3. Why do we need the Repository?

Two lookups: everything (newest first), and everything filtered to one
`status` — used by the admin queue to show "just the NEW ones" without
pulling every message ever submitted.

## 4. Why do we need the Service?

`ContactMessageService` is a small, standard CRUD service: `submit()`
(status always starts at `NEW`), `listAll()` (optionally filtered),
`getById()`, `updateStatus()`. Nothing unusual here compared to earlier
modules - which is itself worth noticing: not every module needs
complicated business rules (compare with Module 4's status lifecycle or
Module 5's compatibility rules).

## 5. Why is this the first controller with a genuinely public endpoint?

Every endpoint since Module 2 has required a valid JWT, EXCEPT
`/api/auth/**` (you can't log in if logging in required being logged in
already). `POST /api/contact` is the second deliberate exception - see
`SecurityConfig`:

```java
.requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
```

Notice this is scoped to POST only. `GET /api/contact`, `GET
/api/contact/{id}`, and `PATCH /api/contact/{id}/status` all fall through
to the general `anyRequest().authenticated()` rule, and are further
restricted to `ROLE_ADMIN` with `@PreAuthorize` on the controller. So:
anyone can SUBMIT a message, but only an admin can READ or manage them.

## 6. Why didn't this get folded into `AdminController`?

Module 7's docs originally said contact-message viewing would land in
`AdminController` once this module existed. On reflection, a "message" is
its own resource with its own lifecycle (submit → review → resolve) - the
same reasoning that gives `DonorController` and `BloodRequestController`
their own space instead of cramming everything into one giant admin
class. `ContactController` handles the full resource, restricted per
endpoint by role; `AdminController`'s `/stats` endpoint now includes a
real `newContactMessages` count, so the dashboard still reflects them
without a second, duplicate set of endpoints.

## 7. What API endpoints exist?

| Method | Path | Who |
|---|---|---|
| POST | `/api/contact` | Anyone - no login required |
| GET | `/api/contact?status=NEW` | ADMIN only (status filter optional) |
| GET | `/api/contact/{id}` | ADMIN only |
| PATCH | `/api/contact/{id}/status` | ADMIN only |

## 8. How does the request flow (submitting a message)?

```
Visitor (not logged in): POST /api/contact  { name, email, subject, message, ... }
        ↓
JwtAuthenticationFilter runs, finds no Authorization header, does nothing
        ↓
SecurityConfig's rule: POST /api/contact -> permitAll() - request proceeds anyway
        ↓
ContactController.submit(dto)
        ↓
ContactMessageService.submit(dto)  - saves with status = NEW
        ↓
Returns the created ContactMessageResponseDto
```

## 9. How does React communicate with the backend?

`ContactPage` (public, no login gate) calls `POST /api/contact` exactly
like the original Express route did. `AdminDashboard`'s message queue
(once built out further) would call the `GET`/`PATCH` endpoints here,
authenticated as an admin.
