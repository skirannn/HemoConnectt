# Module 10: Generative AI Assistant

## 1. What problem does this module solve?

Someone using HemoConnect for the first time might not know what
"MATCHED" means, or how donor matching works, or when they're eligible to
donate again. This module adds a chat assistant that can explain the app
and answer general blood-donation questions - clearly scoped to NOT give
medical advice about any specific person.

## 2. Why does this module have no Entity or table?

Every earlier module persisted something. This one doesn't need to:
`AiAssistantService.chat()` is a stateless request/response call to an
external API (Anthropic) - the frontend is responsible for keeping track
of the conversation so far and sending it back as `conversationHistory`
on the next message. Nothing about a chat needs to survive a server
restart or be queried later, so there's genuinely nothing to store.

## 3. Why `RestClient` instead of a whole SDK?

Spring 6.1 (included in Spring Boot 3.2+) ships `RestClient` - a modern,
synchronous HTTP client - as part of `spring-web`, which was already a
dependency from Module 1. Adding Anthropic's official SDK would pull in
extra dependencies for what's really just one HTTP call with a JSON body.
`AiClientConfig` registers ONE `RestClient` bean (base URL + the fixed
`anthropic-version` header); `AiAssistantService` adds the secret
`x-api-key` header per-request from configuration.

## 4. Where does the "no medical diagnosis" rule actually live?

In `AiAssistantService.SYSTEM_PROMPT` - a fixed block of instructions sent
to the model with EVERY request, telling it explicitly what it can help
with (navigating the app, explaining the request workflow, general public
blood-donation facts) and what it must refuse (diagnosing anyone,
personalized eligibility advice, treatment recommendations - always
redirecting those to a real doctor). This is the actual mechanism the
project brief's requirement ("must NOT provide medical diagnosis or
treatment") is implemented with - it's not just a comment, it's the
literal text the model is instructed with on every call.

## 5. Why does a missing API key not crash the whole app?

Compare this to `JwtService` (Module 2), which throws at STARTUP if
`JWT_SECRET` is missing - JWT is core infrastructure the entire app
depends on, so failing fast and loud is right. The AI assistant is
optional and supplementary (explicitly built last, after everything else
worked). `AiAssistantService.chat()` checks for the key at THE MOMENT
someone tries to use it, and throws `AiNotConfiguredException` (mapped to
a clean 503) - so the rest of the app keeps working perfectly for anyone
who never touches this feature.

## 6. Why does the API key never reach the frontend?

`AI_API_KEY` is read via `@Value` directly into `AiAssistantService` -
nowhere in any DTO, controller response, or frontend code. The browser
only ever talks to `POST /api/ai/chat` on THIS backend; this backend is
the only thing that ever talks to Anthropic. If the key were in the
frontend's JavaScript bundle, anyone could open dev tools and steal it -
keeping it server-side is the entire point of building this as a real
backend call instead of calling the AI API directly from React.

## 7. What API endpoint exists?

| Method | Path | Who |
|---|---|---|
| POST | `/api/ai/chat` | Any logged-in user |

Requires login (falls through to `SecurityConfig`'s default rule) -
there's no reason to let an anonymous visitor run up API costs on an
open endpoint.

## 8. How does the request flow?

```
User: "How long until I can donate again after donating?"
        ↓
POST /api/ai/chat  { message: "...", conversationHistory: [...] }
        ↓
AiController.chat(request)
        ↓
AiAssistantService.chat(request)
        - checks AI_API_KEY is set (throws 503 if not)
        - buildMessages(): conversationHistory + new message, in order
        - RestClient POST to https://api.anthropic.com/v1/messages
          with SYSTEM_PROMPT + x-api-key header
        - extractReplyText(): pulls the text out of the response
        ↓
Returns { reply: "..." }
```

## 9. How does React communicate with the backend?

`services/aiApi.js` (Module 9's pattern) wraps this one endpoint -
`askAssistant(message, conversationHistory)`. No chat UI component exists
in the frontend yet; building one (a simple message list + input box,
storing `conversationHistory` in React state and passing it back on each
call) is a good next exercise using everything Module 9 already set up.
