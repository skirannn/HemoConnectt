# Module 3: Donor

## 1. What problem does the Donor module solve?

`User` (Module 1) tells us someone is a donor, but a real donor needs more
data than that: physical eligibility info, donation history, and — most
importantly — whether they're currently allowed to donate. This module
carries over the single most valuable piece of business logic from the
original project: **a donor can't donate again for 56 days after their
last donation.**

## 2. What is the Entity?

`DonorProfile` is linked to `User` with `@OneToOne` — one donor, one
profile, enforced with a `unique = true` foreign key. We deliberately kept
donor-only fields (age, weight, last donation date, eligibility...) out of
`User` itself, the same separation the original project made by keeping
`DonorBlood` as its own MongoDB collection instead of bloating the `User`
model.

One simplification worth calling out: the original `DonorBlood` also
stored a separate `rhFactor` field. We dropped it here — `User.bloodGroup`
(e.g. `O_POSITIVE`) already encodes the Rh factor, so a second field would
just be data that could silently drift out of sync with the first. Fewer
fields, same information.

## 3. Why do we need the Repository?

`DonorProfileRepository.findByUserId(userId)` is a derived query method —
Spring Data JPA reads the method name and generates
`SELECT * FROM donor_profiles WHERE user_id = ?` for us. No SQL written.

## 4. Why do we need the Service? (the eligibility rule)

`DonorProfileService.recalculateEligibility()` is the heart of this
module:

```
if lastDonationDate is null:
    eligible = true          (never donated -> nothing to cool down from)
else:
    nextEligibleDate = lastDonationDate + 56 days
    eligible = today >= nextEligibleDate
```

This runs every time `recordDonation()` is called, so `eligible` and
`nextEligibleDate` are always freshly computed — never stale. The original
project computed this with a Mongoose `pre('save')` hook, which runs
"invisibly" whenever a document is saved. We chose to write it as an
explicit method call instead: it's slightly more code, but you can read
top-to-bottom exactly when and why the calculation runs, which matters a
lot when you're learning.

## 5. Why do we need the Controller?

`DonorController` exposes the three things a donor (or an admin) can do:
view a profile, create/update it, and record a donation. Every method uses
`@PreAuthorize("#userId == authentication.principal.user.id or
hasRole('ADMIN')")` — this is Spring Security checking, before your
controller code ever runs, that the logged-in user either IS this donor or
is an ADMIN. That's the Java equivalent of the original project's
`requireRole`/`requireRoles` Express middleware, just written per-endpoint
using Spring Expression Language (SpEL) instead of a wrapper function.

## 6. What API endpoints exist?

| Method | Path | Who can call it |
|---|---|---|
| PUT | `/api/donors/{userId}/profile` | That donor, or an ADMIN |
| GET | `/api/donors/{userId}/profile` | That donor, or an ADMIN |
| POST | `/api/donors/{userId}/donations` | That donor, or an ADMIN |

## 7. How does the request flow?

```
PUT /api/donors/7/profile  { age: 28, weight: 65, gender: "MALE", ... }
        ↓
JwtAuthenticationFilter confirms who's logged in
        ↓
@PreAuthorize checks: is the logged-in user's id == 7, or are they ADMIN?
        ↓
DonorController.upsertProfile(7, request)
        ↓
DonorProfileService.createOrUpdateProfile(7, request)
        - loads the User (must have role DONOR)
        - finds an existing DonorProfile or creates a new one
        - copies the request fields onto it, saves
        ↓
DonorProfileRepository.save(...)  → Hibernate → MySQL: INSERT/UPDATE donor_profiles
        ↓
Returns a DonorProfileResponseDto (profile fields + a few read-only User fields)
```

## 8. How does Hibernate communicate with MySQL?

Same mechanism as Modules 1–2, with one new idea: `@OneToOne` +
`@JoinColumn(name = "user_id", unique = true)`. Hibernate stores this as a
plain `user_id` foreign-key column on `donor_profiles`, and the `unique =
true` constraint is what actually enforces "at most one profile per user"
at the database level — not just in our Java code.

## 9. How does React communicate with the backend?

Once Module 9 connects the frontend, the existing `ProfilePage` /
`ProfileSetupPage` (for donors) and `DonorHistoryPage` will call these
three endpoints instead of the old `/api/donor-blood/**` Express routes.
The JSON shape returned (`eligible`, `nextEligibleDate`,
`totalDonations`, ...) is designed to map directly onto what those pages
already display.
