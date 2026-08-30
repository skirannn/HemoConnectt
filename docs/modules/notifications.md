# Module 6: Notifications

## 1. What problem does the Notifications module solve?

Blood requests move through a lot of state changes (Module 4) - a request
gets a response, a donor gets confirmed, a request gets cancelled - and
the person affected needs to find out about it. This module is the
"inbox" that captures those events.

## 2. What is the Entity?

`Notification` is a simple table: who it's for (`user`), what kind of
event it was (`type`), a title/message to display, and whether it's been
read. Nothing donor- or recipient-specific about the entity itself - any
user can receive any notification type.

**REST-only, on purpose**: the original project also pushed notifications
live over Socket.IO. The project brief explicitly says real-time delivery
can be added later, so this module only builds create + list +
mark-as-read. A frontend can still get "near real-time" updates cheaply by
polling `GET /api/notifications/unread-count` every so often.

## 3. Why do we need the Repository?

Three small derived queries: all of a user's notifications (newest
first), a count of unread ones (for a notification-bell badge), and the
unread ones specifically (used internally by "mark all as read" so we
don't bother re-saving notifications that are already read).

## 4. Why do we need the Service? (and why it's called FROM other services)

`NotificationService.notify(...)` is deliberately not tied to any single
controller. There's no "create a notification" button in the app -
notifications only ever happen as a SIDE EFFECT of something else. That's
why `BloodRequestService` (Module 4) calls
`notificationService.notify(...)` directly, at four points:

| Event in BloodRequestService | Who gets notified | Type |
|---|---|---|
| A new request is created | Every matching, eligible donor (via Module 5's `DonorMatchingService`) | `NEW_MATCHING_REQUEST` |
| A donor responds | The requester | `DONOR_RESPONSE` |
| The requester confirms a donor | That donor | `STATUS_CHANGE` |
| A request is fulfilled | The confirmed donor | `STATUS_CHANGE` |
| A request is cancelled | The confirmed donor, or everyone who'd accepted | `STATUS_CHANGE` |

This is the clearest example yet of **services composing**: creating a
request now touches three services (`BloodRequestService` →
`DonorMatchingService` → `NotificationService`), and none of them need to
know how the others work internally - each just calls the next one's
public method.

One safety detail worth noticing: the matching-donor notification loop in
`createRequest()` is wrapped in a `try/catch`. If matching or notifying
fails for any reason, the blood request itself still gets created
successfully - a notification failure should never be allowed to break
the actual feature the user asked for.

## 5. Why do we need the Controller?

`NotificationController` never takes a `{userId}` in the URL, unlike
`DonorController`. Notifications are inherently personal, so every
endpoint just reads "who am I?" from the JWT
(`@AuthenticationPrincipal`) instead of trusting an id the client could
put in the URL — a slightly stronger, simpler pattern than the
self-or-admin checks used elsewhere.

## 6. What API endpoints exist?

| Method | Path | Description |
|---|---|---|
| GET | `/api/notifications` | My notifications, newest first |
| GET | `/api/notifications/unread-count` | For a notification-bell badge |
| PATCH | `/api/notifications/{id}/read` | Mark one as read (must be mine) |
| PATCH | `/api/notifications/read-all` | Mark everything as read |

## 7. How does the request flow (for the "donor responded" case)?

```
Donor: POST /api/blood-requests/10/respond  { responseType: "ACCEPT" }
        ↓
BloodRequestService.respondToRequest(...)
        - saves the DonorResponse, updates status
        - notificationService.notify(requester, DONOR_RESPONSE, ...)
                ↓
                NotificationRepository.save(...) → new row in `notifications`
        ↓
Later, the requester: GET /api/notifications
        ↓
NotificationController.getMyNotifications(principal)
        ↓
NotificationService.listForUser(requesterId)
        ↓
Returns the new notification in the list
```

## 8. How does Hibernate communicate with MySQL?

Nothing new here beyond what earlier modules covered - one `@ManyToOne`
to `User`, one table. Worth noticing, though: `markAllAsRead()` uses
`saveAll(...)` on a `List<Notification>` instead of calling `save()` in a
loop - one batch operation instead of N separate ones.

## 9. How does React communicate with the backend?

`NotificationsPage` calls `GET /api/notifications` for the list and
`PATCH /api/notifications/{id}/read` when a notification is opened; a
notification bell icon elsewhere in the UI can poll
`GET /api/notifications/unread-count` on an interval for a badge count -
all without needing WebSockets yet.
