# Module 7: Admin

## 1. What problem does the Admin module solve?

Every other module answers a question for one specific user (my profile,
my requests, my notifications). Admin answers questions about the WHOLE
system: how many users do we have, which requests need review, and how do
we handle bad actors.

## 2. Is there a new Entity?

No, and that's worth noticing explicitly. Admin isn't its own "thing" in
the data model - it's a role (`Role.ADMIN`, from Module 1) with extra
permissions layered on top of entities that already exist. The one small
addition is two new columns on `BloodRequest` (Module 4):
`isFlagged`/`flagReason`, for moderation.

## 3. Why does `AdminService` look different from the others?

Every other service in this project (`UserService`, `DonorProfileService`,
`BloodRequestService`, ...) owns exactly one entity. `AdminService` is
different on purpose: it **coordinates** three other services
(`UserService`, `DonorProfileService`, `BloodRequestService`) plus two
repositories used directly for simple counts
(`userRepository.countByRole(...)`). This is a normal, common pattern for
an admin/reporting layer - it doesn't own data, it aggregates it.

**A real bug fix, not just a rebuild**: the original project's admin
stats endpoint returned a hardcoded `completedDonations: 45` — literal
placeholder data. `getStatistics()` here queries the real count instead
(`bloodRequestRepository.countByStatus(FULFILLED)`). The project brief
was explicit about this: *"Do not claim ... functionality if the code
does not implement it"* — a fake number in a stats dashboard is exactly
that.

## 4. Why is `@PreAuthorize` on the class instead of each method?

`AdminController` is the first controller where **every single endpoint**
needs the same rule: `hasRole('ADMIN')`. Putting `@PreAuthorize` on the
class instead of repeating it on every method is a straightforward
readability win here — but it's a rule you should only take when it's
genuinely true for every method in the file. Compare this with
`BloodRequestController`, where different endpoints have different rules
(`RECIPIENT` here, `DONOR` there), so each is annotated individually.

## 5. Why isn't "view contact messages" here?

Because Module 8 (Contact) doesn't exist yet. The original admin
dashboard could view submitted contact messages, but building that here
would mean creating a fake/partial version of a feature that belongs to a
module we haven't built. Per the project brief - *"do not fake
completion"* - this gets added to `AdminController` once Module 8 is
actually done, not before.

## 6. What API endpoints exist?

Every endpoint below requires `ROLE_ADMIN`.

| Method | Path | Description |
|---|---|---|
| GET | `/api/admin/stats` | Real aggregate counts for the dashboard |
| GET | `/api/admin/users` | Every user, newest first |
| DELETE | `/api/admin/users/{id}` | Remove a user account |
| GET | `/api/admin/donors` | Every donor profile |
| GET | `/api/admin/blood-requests` | Every request, any status |
| GET | `/api/admin/blood-requests/flagged` | Requests awaiting review |
| POST | `/api/admin/blood-requests/{id}/flag` | Flag a request, with a reason |
| POST | `/api/admin/blood-requests/{id}/approve` | Clear a flag - request continues normally |
| POST | `/api/admin/blood-requests/{id}/reject` | Cancel a flagged request and notify the requester |

## 7. How does the request flow (rejecting a flagged request)?

```
Admin: POST /api/admin/blood-requests/12/reject  { reason: "Confirmed fraudulent" }
        ↓
@PreAuthorize (class-level) checks: does this user have ROLE_ADMIN?
        ↓
AdminController.rejectRequest(12, dto)
        ↓
AdminService.rejectFlaggedRequest(12, reason)
        ↓
BloodRequestService.rejectFlaggedRequest(12, reason)   <- the actual business rule lives HERE
        - checks the request is actually flagged
        - status -> CANCELLED, flag cleared
        - notificationService.notify(requester, STATUS_CHANGE, ...)
        ↓
Returns the updated BloodRequestResponseDto
```

Notice `AdminService` doesn't implement the rejection rule itself - it
delegates straight to `BloodRequestService`, which already owns all
`BloodRequest` business logic (Module 4). Admin doesn't duplicate rules,
it reuses them with elevated permission to call them on ANY request, not
just your own.

## 8. How does Hibernate communicate with MySQL?

Nothing new beyond earlier modules - two more columns on an existing
table, and simple `COUNT(*)` queries (`countByRole`, `countByStatus`,
`countByFlaggedTrue`) generated the same derived-method way as every
other count/exists query in this project.

## 9. How does React communicate with the backend?

`AdminDashboard` calls `/api/admin/stats` for the overview cards, and the
flagged-requests endpoints for the moderation queue - the same shape the
original project's admin dashboard already expected, just backed by real
data end to end.
