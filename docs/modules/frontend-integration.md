# Module 9: React ↔ Spring Boot Integration

## 1. What problem does this module solve?

Every module so far built a real, working piece of the backend. This
module connects your EXISTING React UI to it, replacing the old Express
server as the thing it talks to - ideally with as few changes to the
React code as possible, since that code already works and you already
understand it.

## 2. What's actually wired up right now (be precise about scope)

This pass fully connects the **authentication flow** end to end:
signup, login, session restore on page load, logout, and the
forgot-password OTP flow. That's the backbone every other page depends on
(`ProtectedRoute` can't work without it). The remaining feature pages
(dashboards, blood requests, donor matching, notifications, admin,
contact, profile editing) are **not yet migrated** - Section 8 below is a
precise, practical guide to finishing them yourself, using the exact same
techniques this module already applied. This is deliberate, not an
oversight: doing all ~15 remaining pages "for" you, without you tracing
each one's exact field shapes the way this module did, would risk
teaching you nothing and leaving subtle bugs you can't debug. Practicing
the same pattern a few more times is where the real learning happens.

## 3. The single biggest integration trick: making the backend speak the frontend's language

Instead of rewriting the frontend's field names everywhere, two backend
enums were changed to serialize/deserialize in the EXACT format the
frontend already sends and expects:

```java
// Role.java
@JsonValue
public String toJson() { return name().toLowerCase(); }   // DONOR -> "donor"

@JsonCreator
public static Role fromJson(String value) { return Role.valueOf(value.toUpperCase()); }
```

```java
// BloodGroup.java
@JsonValue
public String getLabel() { return label; }   // A_POSITIVE -> "A+"

@JsonCreator
public static BloodGroup fromLabel(String label) { ... }   // "A+" -> A_POSITIVE
```

This means `user.role === 'donor'` and blood group dropdowns showing
`"A+"` throughout the ENTIRE existing frontend just work, with zero
changes - even on pages we haven't touched yet. Two annotations replaced
what would otherwise have been dozens of little translation snippets
scattered across the codebase. (Note: `@Enumerated(EnumType.STRING)`,
which controls how these enums are stored in MySQL, is unaffected - the
database still stores `"DONOR"`/`"A_POSITIVE"`, only the JSON layer
changed.)

## 4. Why a centralized service layer (`src/services/`)?

The project brief asked for this explicitly ("Use a centralized
API/service layer"). `httpClient.js` is the ONE place that knows how to
attach the JWT and how to read the backend's error shape
(`{ message: "..." }` from `GlobalExceptionHandler`). `authApi.js` wraps
every real `/api/auth/**` endpoint. Every future service file
(`donorApi.js`, `bloodRequestApi.js`, ...) should follow the same shape:
thin functions that just describe "what URL, what method, what body" -
never business logic.

## 5. Why `mappers.js` instead of renaming backend fields?

Two fields genuinely differ: the backend's `profileCompleted` (Module 1)
vs. the frontend's `profileComplete`, and `availableForDonation` vs.
`isAvailable`. Renaming the BACKEND to match old frontend naming would
mean permanently carrying awkward names for a project you're supposed to
be able to explain in an interview. Instead, `normalizeUser()` in
`mappers.js` adds the old names as aliases, once, in `AuthContext` -  the
one place a user object enters the app. Every existing page reading
`user.profileComplete` keeps working, unmodified.

## 6. Why does Vite's dev proxy matter here?

```js
// vite.config.js
proxy: { '/api': { target: 'http://localhost:8080', ... } }
```

Every `fetch('/api/...')` call anywhere in this codebase (old or
unmigrated) is a *relative* path. Vite's dev server forwards anything
starting with `/api` straight to Spring Boot. Changing this ONE line
(the port, from the old Express server's 5000 to Spring Boot's 8080) is
what makes the whole app point at the new backend during `npm run dev` -
no per-file URL changes needed anywhere.

`CorsConfig.java` (backend) exists for the cases the proxy doesn't cover
- `npm run preview`, a production build served from its own domain, or
any tool that calls the API directly from a browser context.

## 7. What else had to change, and why

- **`application.yml`: `fail-on-unknown-properties: false`** - the
  signup form sends a `confirmPassword` field that only matters
  client-side. Without this, Spring rejects the entire signup request
  just for containing one field our DTO doesn't declare.
- **`SocketContext.jsx` → no-op stub** - the original pushed live
  notifications over Socket.IO; this backend doesn't run a Socket.IO
  server (Module 6 deliberately deferred real-time delivery). The stub
  keeps the same `useSocket()` shape so nothing crashes; real-time
  polling via `GET /api/notifications/unread-count` is available today
  as the interim replacement.
- **`ForgotPasswordPage.jsx`** - dropped the "reset via phone" radio
  option. Module 2's backend only ever implemented the email path for
  real (the original's "phone" option used the identical in-memory OTP
  logic behind the scenes - nothing real was actually lost).
- **`package.json`** - trimmed to frontend-only dependencies (removed
  `express`, `mongoose`, `bcryptjs`, `jsonwebtoken`, `socket.io`, `cors`
  - all backend/Express-only packages that don't belong in a Vite app -
  plus `socket.io-client`, unused now).

## 8. Migrating the rest: pattern + endpoint map

For each remaining page, the process used in this module is: **(1)** find
its `fetch()` calls, **(2)** check the exact path/payload shape below,
**(3)** move the call into a new `src/services/xxxApi.js` file following
`authApi.js`'s shape, **(4)** update the page to call that function
instead of `fetch()` directly.

| Page | Old call (Express) | New call (Spring Boot) | Notes |
|---|---|---|---|
| `ProfileSetupPage` | `POST /api/users/complete-profile` | `PUT /api/users/{id}/profile` (Module 1) + `PUT /api/donors/{userId}/profile` (Module 3, donors only) | Two calls now instead of one - basic profile fields vs. donor-only fields live in different tables |
| `ProfileSetupPage` | `POST /api/users/save-draft` | *(no equivalent yet)* | Not built - the new backend doesn't have a "draft" concept; profile updates are all-or-nothing via Module 1's endpoint |
| `ProfilePage` | `PUT /api/users/profile` | `PUT /api/users/{id}/profile` | Needs the logged-in user's own id in the path now |
| `ProfilePage` | `PUT /api/users/update-password` | *(no equivalent yet)* | Not built - would need a small new endpoint (e.g. `PUT /api/users/{id}/password`, verifying the old password) - a good exercise using the exact patterns from Module 1 |
| `ProfilePage` | `POST /api/upload/profile-pic` | *(no equivalent)* | File uploads were never in any module's scope |
| `DonorDashboard` | `PUT .../availability` (toggle) | `PUT /api/donors/{userId}/profile` | Availability now lives on `User.availableForDonation`, set via Module 1's profile update |
| `BloodRequestPage` | `POST /api/blood-requests` | `POST /api/blood-requests` | Path matches! Check field names: `unitsRequired` (not `units`), `requiredDate` (not `requiredByDate`), `urgency` is uppercase (`"CRITICAL"`, not `"critical"`) |
| `DonorMatchingPage` | `GET /api/blood-requests/nearby` | `GET /api/blood-requests/{id}/matches` (Module 5) | Different shape entirely - matches are now per-request, not a general nearby-requests feed |
| `DonorMatchingPage` (respond) | `POST /api/blood-requests/:id/respond` | `POST /api/blood-requests/{id}/respond` | Path matches; `responseType` is uppercase (`"ACCEPT"`, not `"accept"`) |
| `NotificationsPage` | `GET /api/notifications` | `GET /api/notifications` | Path matches; no `{userId}` needed - the backend reads it from the JWT |
| `AdminDashboard` | `GET /api/admin/stats` | `GET /api/admin/stats` | Path matches; `completedDonations` field is now `fulfilledRequests`, and it's a REAL count (Module 7) |
| `ContactPage` | `POST /api/contact` | `POST /api/contact` | Path and shape match closely - this one should need almost no changes |
| All list pages | Mongo `_id` on nested items (donors, requests, etc.) | `id` | `mappers.js`'s pattern (alias the old name) is the fastest fix - or just update each `.map(x => x._id)` to `.map(x => x.id)` |

Status/urgency/response-type enums throughout the app come back
**UPPERCASE** from every module except the two fixed in Section 3
(`Role`, `BloodGroup`). If you want the same "no frontend changes needed"
effect for `RequestStatus`, `UrgencyLevel`, `ResponseType`, etc., apply
the identical `@JsonValue`/`@JsonCreator` pattern to those enums too.

## 9. How does the request flow (a fully wired example: login)?

```
LoginPage: login(email, password, rememberMe)   <- unchanged, calls useAuth()
        ↓
AuthContext.login()
        ↓
authApi.login(email, password, rememberMe)
        ↓
httpClient.apiRequest('/api/auth/login', { method: 'POST', body, skipAuth: true })
        ↓
fetch('/api/auth/login', ...)   <- relative path
        ↓
Vite dev server proxy: /api/** -> http://localhost:8080
        ↓
Spring Boot: AuthController.login() -> AuthService.login() (Module 2)
        ↓
Response: { token, user: { role: "donor", bloodGroup: "O+", profileCompleted: false, ... } }
        ↓
AuthContext: localStorage.setItem('token', ...), normalizeUser(user) adds profileComplete/isAvailable/_id
        ↓
ProtectedRoute reads user.profileComplete -> redirects to /profile-setup (unmodified code, still works)
```
