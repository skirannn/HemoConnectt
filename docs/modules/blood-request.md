# Module 4: Blood Request

## 1. What problem does the Blood Request module solve?

This is the actual core of HemoConnect: a recipient asks for blood, donors
respond, the recipient picks one, and (eventually) the donation happens.
Everything else in the app (matching, notifications, admin moderation)
exists to support this one workflow.

## 2. What are the Entities?

Two entities, mirroring the original project's design:

- **`BloodRequest`** — one row per request: who needs blood, what blood
  group, how urgently, and its current `status`.
- **`DonorResponse`** — one row per donor's reaction (accept/decline/maybe)
  to a specific request.

The original MongoDB model stored responses as an **embedded array**
inside the `BloodRequest` document. A relational database doesn't really
have "an array of objects living inside another row" — the natural way to
represent that is a separate table with a foreign key back to the parent,
which is exactly what `DonorResponse.bloodRequest` (`@ManyToOne`) does.
`BloodRequest.responses` (`@OneToMany(mappedBy = "bloodRequest")`) is the
other side of that same relationship — Hibernate uses it to load "all the
responses for this request" as a normal Java `List`.

**Simplification vs. the original:** the original status machine had 7
states, including a separate `in_progress` between `matched` and
`confirmed`. We merged that into `CONFIRMED` — once a specific donor is
confirmed, the request genuinely IS in progress; tracking that as two
different states didn't add real meaning, just more branches to reason
about.

## 3. Why do we need the Repository?

Two derived query methods do all the lookups this module needs:
`findByStatusInOrderByCreatedAtDesc(...)` (for the "active requests" list)
and `findByRequesterIdOrderByCreatedAtDesc(...)` (for "my requests").
`DonorResponseRepository.existsByBloodRequestIdAndDonorId(...)` answers
one specific question — "has this donor already responded?" — used to
stop duplicate responses.

## 4. Why do we need the Service? (the interesting rules)

`BloodRequestService` carries every real business rule from the original
project:

- **Auto-match on accept**: the exact same rule as the original's
  `addResponse()` — if a donor `ACCEPT`s a `PENDING` request, the status
  automatically becomes `MATCHED`. No separate "match" button.
- **Confirm requires acceptance**: `confirmDonor()` checks the chosen
  donor actually has an `ACCEPT` response on file before letting the
  requester confirm them.
- **Fulfilling calls into Module 3**: `fulfillRequest()` doesn't just flip
  a status — it calls `donorProfileService.recordDonation(...)`, so the
  confirmed donor's 56-day cooldown (Module 3) kicks in automatically the
  moment a request is fulfilled. This is a good example of **one service
  calling another** — `BloodRequestService` doesn't know or care HOW
  eligibility is calculated, it just asks `DonorProfileService` to handle it.
- **Lazy expiry**: instead of a Mongo TTL index (which just silently
  deletes documents), we check "has 30 days passed?" every time a request
  is loaded or listed, and flip it to `EXPIRED` right then
  (`markExpiredIfNeeded`). `ExpiredRequestScheduler` additionally sweeps
  once an hour with Spring's `@Scheduled`, so a request nobody happens to
  view still gets cleaned up eventually — one annotation, no extra library.

## 5. Two different kinds of authorization (an interview-worthy distinction)

`@PreAuthorize("hasRole('DONOR')")` on the controller checks something
known **before** the method runs — the caller's role. It can't check "is
this actually your request?" — that requires loading the specific
`BloodRequest` row first. That's why ownership checks
(`assertRequesterOrAdmin`) live **inside the service**, after the entity
is loaded, throwing a plain `AccessDeniedException` that
`GlobalExceptionHandler` turns into a 403.

## 6. What API endpoints exist?

| Method | Path | Who | Description |
|---|---|---|---|
| POST | `/api/blood-requests` | RECIPIENT | Create a request |
| GET | `/api/blood-requests/{id}` | Any logged-in user | View one request + its responses |
| GET | `/api/blood-requests/active` | Any logged-in user | List open (PENDING/MATCHED) requests |
| GET | `/api/blood-requests/mine` | RECIPIENT | The requester's own history |
| POST | `/api/blood-requests/{id}/respond` | DONOR | Accept/decline/maybe |
| POST | `/api/blood-requests/{id}/confirm` | The requester or ADMIN | Pick one accepted donor |
| POST | `/api/blood-requests/{id}/fulfill` | The requester or ADMIN | Mark the donation completed |
| POST | `/api/blood-requests/{id}/cancel` | The requester or ADMIN | Cancel the request |

## 7. How does the request flow (accepting a request, as an example)?

```
Donor: POST /api/blood-requests/10/respond  { responseType: "ACCEPT" }
        ↓
@PreAuthorize checks: does this user have ROLE_DONOR?
        ↓
BloodRequestController.respond(10, donorPrincipal, dto)
        ↓
BloodRequestService.respondToRequest(10, donorId, dto)
        - loads the BloodRequest, checks it's still open
        - checks this donor hasn't already responded
        - creates + attaches a new DonorResponse
        - status PENDING -> MATCHED (because this was an ACCEPT)
        ↓
bloodRequestRepository.save(request)
        → cascade = ALL also inserts the new DonorResponse row
        ↓
Returns the updated BloodRequestResponseDto, including the full responses list
```

## 8. How does Hibernate communicate with MySQL?

New idea introduced here: `@OneToMany(mappedBy = "bloodRequest", cascade =
CascadeType.ALL, orphanRemoval = true)`. `mappedBy` tells Hibernate "the
foreign key lives on the OTHER side (`DonorResponse.bloodRequest`) — don't
create a separate join table." `cascade = ALL` means we never have to call
`donorResponseRepository.save(...)` by hand — saving the parent
`BloodRequest` automatically saves any new/changed `DonorResponse`s in its
`responses` list.

## 9. How does React communicate with the backend?

`BloodRequestPage` (create), `DonorMatchingPage` (browse
`/active`, respond) and `RecipientHistoryPage`/`DonorHistoryPage`
(`/mine`) all map directly onto the endpoints above — same idea as every
other module, only the base URL changes once Module 9 wires up the
frontend.
