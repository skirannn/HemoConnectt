# Module 5: Donor Matching

## 1. What problem does the Donor Matching module solve?

Once a recipient creates a blood request (Module 4), someone needs to
figure out WHICH donors to show it to. This module answers exactly one
question: "for this specific request, who is a real, eligible, compatible
candidate donor?"

## 2. Is there a new Entity?

No — this is the first module that's pure logic on top of data that
already exists. It reads `BloodRequest` (Module 4) and `DonorProfile`
(Module 3) but doesn't introduce a new table.

## 3. Why do we need `BloodCompatibility`?

Real blood donation compatibility isn't "same blood group only." An O-
donor can give to literally anyone; an AB+ recipient can receive from
anyone. `BloodCompatibility` is a single static lookup table (recipient
blood group → the set of donor blood groups allowed to give to them). It's
not "a complicated algorithm" — it's one `Map`, no loops, no external
data — but it makes the matching results medically accurate instead of
artificially narrow, which is worth the ~15 extra lines.

## 4. Why do we need the Repository query?

`DonorProfileRepository.findEligibleMatches(bloodGroups, location)` is one
JPQL query (written with `@Query`, since this filter combination is too
specific for a derived method name to stay readable) that does all three
structural filters in one round-trip to the database: `eligible = true`,
`availableForDonation = true`, blood group in the compatible set, and a
simple case-insensitive location substring match — the same level of
"just match on text" the original project used, rather than real
geo-distance math.

## 5. Why do we need the Service?

`DonorMatchingService.findMatches()` does three things, in order:

1. **Loads the request** and checks the caller is either the requester or
   an admin (matches results include donor phone numbers — real contact
   info — so this can't be open to just anyone).
2. **Skips matching entirely** if the request is already `CONFIRMED`,
   `FULFILLED`, `CANCELLED`, or `EXPIRED` — there's nothing to match
   against anymore.
3. **Filters out "emergency only" donors** (Module 3's `emergencyOnly`
   flag) unless the request's urgency is `CRITICAL`. A donor who only
   wants to be bothered for true emergencies shouldn't be surfaced for a
   routine `MEDIUM` request.

## 6. What API endpoint exists?

| Method | Path | Who |
|---|---|---|
| GET | `/api/blood-requests/{id}/matches` | The requester, or an ADMIN |

It lives on `BloodRequestController` rather than a brand new controller —
"the matches for this request" is naturally a sub-resource of the request
itself, the same way you'd design this as a REST API by hand.

## 7. How does the request flow?

```
Requester: GET /api/blood-requests/5/matches
        ↓
BloodRequestController.getMatches(5, principal)
        ↓
DonorMatchingService.findMatches(5, caller)
        - loads BloodRequest 5, checks caller owns it (or is admin)
        - looks up compatible donor blood groups for request.bloodGroup
        - DonorProfileRepository.findEligibleMatches(compatibleGroups, "Hyderabad")
          → Hibernate → MySQL: one JOINed SELECT across donor_profiles + users
        - filters out emergency-only donors (unless request is CRITICAL)
        ↓
Returns a List<MatchedDonorDto> - donor name, phone, blood group, eligibility info
```

## 8. How does Hibernate communicate with MySQL?

The `@Query` on `findEligibleMatches` is written in **JPQL** (Java
Persistence Query Language) — it looks like SQL, but it refers to entity
names and Java field names (`dp.user.bloodGroup`) instead of table/column
names. Hibernate translates that into a real SQL `JOIN` between
`donor_profiles` and `users` at runtime. This is the first query in the
project where a derived method name (like `findByEmail`) would have gotten
too long and unreadable — a good moment to introduce `@Query` explicitly.

## 9. How does React communicate with the backend?

`DonorMatchingPage` calls `GET /api/blood-requests/{id}/matches` to show a
recipient the list of candidate donors for their own request, which they
can then act on via Module 4's `/respond` (as a donor) and `/confirm` (as
the requester) endpoints — matching (this module) is deliberately kept
separate from responding/confirming (Module 4), since they're different
concerns: "who's a possible match" vs. "what a specific person decided to do."
