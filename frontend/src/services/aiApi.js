// Module 10: thin wrapper around POST /api/ai/chat, following the same
// pattern as authApi.js (Module 9) - no business logic here, just the
// URL/method/body for the one real AI endpoint. No chat UI component
// exists yet; this is the integration point a future NotificationsPage-
// style component would call into.
import { apiRequest } from './httpClient';

/**
 * @param {string} message - the user's new question.
 * @param {{role: 'user'|'assistant', content: string}[]} conversationHistory
 *   - earlier turns in the conversation, oldest first. Optional.
 */
export function askAssistant(message, conversationHistory = []) {
  return apiRequest('/api/ai/chat', {
    method: 'POST',
    body: { message, conversationHistory },
  });
}
