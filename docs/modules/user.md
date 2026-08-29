# Module 1: User Management

## 1. What problem does the User module solve?

Every feature in HemoConnect — donating blood, requesting blood,
notifications, admin moderation — is tied to *who* is using the app. The
User module is the foundation: it defines what an account looks like and
gives every other module a `User` (or a `User.id`) to attach data to.

## 2. What is the Entity?

`User.java` is a plain Java class annotated with `@Entity`. Hibernate reads
those annotations and, at startup, creates (or updates) a matching `users`
table in MySQL — one column per field. `@Id` + `@GeneratedValue` tell
Hibernate the primary key is an auto-incrementing number, just like MySQL's
`AUTO_INCREMENT`.

We store `role` and `bloodGroup` as Java `enum`s (fixed sets of values)
instead of plain strings, so the compiler catches typos like `"Donor"`
(capital D) that would otherwise silently create a broken account.

## 3. Why do we need the Repository?

`UserRepository` is an *interface* — we never write a class that
implements it. Spring Data JPA generates the implementation automatically
at startup by reading:
- The generic type `JpaRepository<User, Long>` → gives us `save()`,
  `findById()`, `findAll()`, `delete()` for free.
- Method names like `findByEmail(String email)` → Spring parses the name
  and builds the SQL `WHERE email = ?` query itself. No SQL string
  anywhere in our code.

## 4. Why do we need the Service?

`UserService` holds the actual business rules: "an email must be unique",
"a password must be hashed before saving", "updating a profile marks it
complete". The controller is not allowed to know any of this — it just
calls `userService.createUser(...)`. This separation means:
- We can reuse the same rule from multiple controllers later (e.g. the
  future `AuthController` will call `userService.createUser(...)` too).
- We can unit-test the rule (see `UserServiceTest`) without starting a web
  server or a database.

## 5. Why do we need the Controller?

`UserController` is the only class in this module that knows about HTTP.
It turns `GET /api/users/5` into a call to `userService.getUserById(5L)`
and turns the returned DTO into a JSON response. If we ever added a second
frontend (a mobile app, say), it would talk to this exact same controller
— none of the business logic would need to change.

## 6. What API endpoints exist?

| Method | Path | Description |
|---|---|---|
| GET | `/api/users/{id}` | Fetch one user's public profile |
| GET | `/api/users` | List all users |
| PUT | `/api/users/{id}/profile` | Update editable profile fields, marks `profileCompleted = true` |
| DELETE | `/api/users/{id}` | Delete a user |

None of these require login yet — that arrives in Module 2.

## 7. How does the request flow?

```
React fetch('/api/users/5')
        ↓
UserController.getUserById(5)
        ↓
UserService.getUserById(5)
        ↓
UserRepository.findById(5)   →  Hibernate  →  MySQL: SELECT * FROM users WHERE id = 5
        ↓
UserService wraps the User entity in a UserResponseDto (no password field)
        ↓
Controller returns it, Spring converts the DTO object to JSON automatically
        ↓
React receives clean JSON
```

## 8. How does Hibernate communicate with MySQL?

Hibernate is an ORM (Object-Relational Mapper) — it translates Java method
calls (`userRepository.save(user)`) into SQL (`INSERT INTO users (...)
VALUES (...)`) and back (a `SELECT` result set becomes a `User` object).
`spring.jpa.hibernate.ddl-auto=update` in `application.yml` also tells
Hibernate to create/alter the `users` table automatically based on the
`User` entity, so you never had to run `CREATE TABLE` by hand while
learning.

## 9. How does React communicate with the backend?

Once Module 9 wires up the frontend, React will call these endpoints with
`fetch`/`axios` against `http://localhost:8080/api/users/...`, exactly the
same way the original app called its Express routes at
`http://localhost:5000/api/users/...` — only the backend implementation
changes, not the contract React relies on.
