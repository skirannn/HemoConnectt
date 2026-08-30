# Module 2: Authentication + JWT + Spring Security

## 1. What problem does the Auth module solve?

Module 1 gave us a `users` table, but nothing stopped anyone from reading
or editing anyone else's data. Auth solves "who is making this request,
and are they allowed to?" — via signup, login, and a JWT that proves
identity on every future request without the server having to remember
sessions.

## 2. What changed from the original project?

| Original (Express) | New (Spring Boot) | Why |
|---|---|---|
| `bcrypt.hash(password, 12)` called by hand in the route | `PasswordEncoder.encode(...)` (Module 1's bean), still BCrypt under the hood | Same algorithm, Spring's standard interface |
| `jwt.sign(...)` / `jwt.verify(...)` called by hand | `JwtService` (this module) | Centralizes all JWT logic in one class |
| `JWT_SECRET \|\| 'your-secret-key'` (insecure fallback) | App **refuses to start** if `JWT_SECRET` isn't set | Closes a real security hole from the original |
| `authenticateToken` Express middleware | `JwtAuthenticationFilter` (a `OncePerRequestFilter`) | Same job, Spring's standard extension point |
| In-memory `otpStore = {}` object | `password_reset_otps` MySQL table | Survives a server restart; works with more than one server instance |
| OTP returned in the API response "for demo" | OTP only logged to the server console | An OTP that's handed back in the response defeats the point of the OTP |

## 3. Why do we need Spring Security's `UserDetails`/`UserDetailsService`?

Spring Security doesn't know our `User` entity — it's designed to work with
ANY user model, so it defines its own tiny interface, `UserDetails`
(username, password, authorities/roles). `UserPrincipal` wraps our `User`
to satisfy that interface, and `CustomUserDetailsService` is the lookup
function Spring Security calls (by email) whenever it needs to check who
someone is.

## 4. Why a `JwtAuthenticationFilter`?

Servlet **filters** run on every request before it reaches a controller.
Ours reads the `Authorization: Bearer <token>` header, and if the token is
valid, tells Spring Security "treat this request as coming from this
user" by populating the `SecurityContext`. `SecurityConfig` registers this
filter to run *before* Spring Security's own username/password filter, so
by the time authorization rules are checked, we already know who's asking.

## 5. Why `AuthenticationManager` instead of comparing passwords ourselves?

We could write `if (passwordEncoder.matches(raw, hash))` by hand — but
Spring Security already has a well-tested component for exactly this,
`DaoAuthenticationProvider`, wired up in `SecurityConfig`. Calling
`authenticationManager.authenticate(...)` in `AuthService.login()` reuses
that instead of reinventing it, and it automatically throws
`BadCredentialsException` on a wrong password, which
`GlobalExceptionHandler` turns into a clean 401.

## 6. What API endpoints exist?

| Method | Path | Auth required? | Description |
|---|---|---|---|
| POST | `/api/auth/signup` | No | Register, returns `{token, user}` |
| POST | `/api/auth/login` | No | Log in, returns `{token, user}` |
| GET | `/api/auth/verify` | Yes | Returns the current user for a valid token |
| POST | `/api/auth/forgot-password` | No | Confirms the account exists |
| POST | `/api/auth/send-otp` | No | Generates a 6-digit OTP, logs it server-side |
| POST | `/api/auth/reset-password` | No | Verifies OTP + sets a new password |

**Every other endpoint in the whole app now requires a valid JWT** — see
`SecurityConfig`'s `anyRequest().authenticated()` rule. That's a
side-effect of this module: Module 1's `UserController` endpoints, which
were wide open before, are now protected automatically.

## 7. How does the request flow for a protected endpoint?

```
React: fetch('/api/users/5', { headers: { Authorization: 'Bearer <token>' } })
        ↓
JwtAuthenticationFilter reads the header, validates the token,
        loads the User via CustomUserDetailsService,
        populates SecurityContext
        ↓
SecurityConfig's authorizeHttpRequests rule: "authenticated()" - passes,
        because SecurityContext now has an authenticated user
        ↓
UserController.getUserById(5) runs normally
```

If the token is missing, expired, or tampered with, the filter simply
never populates the `SecurityContext`, the authorization rule fails, and
Spring Security itself returns a 401/403 — the controller code never runs.

## 8. How does Hibernate/MySQL fit in here?

Two new things persist: the `password_reset_otps` table (Module 2's own
new entity), and every login re-reads the `users` table via
`CustomUserDetailsService` → `UserRepository.findByEmail(...)`. No new ORM
concepts beyond what Module 1 already covered — Hibernate just maps one
more entity.

## 9. How does React communicate with the backend?

Unchanged shape from the original app on purpose: `POST /api/auth/login`
still expects `{ email, password, rememberMe }` and returns `{ token,
user }`; `POST /api/auth/signup` still returns the same shape. `AuthContext.jsx`
(Module 9) will be able to point at this backend with no payload changes —
only the base URL changes from the old Express server to this one.
