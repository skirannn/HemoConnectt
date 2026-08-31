// Module 9: React <-> Spring Boot integration.
//
// A single, centralized place every API call goes through, as asked for
// in the project brief ("Use a centralized API/service layer"). Every
// other file under src/services/ (authApi.js, etc.) calls apiRequest()
// instead of using fetch() directly.
//
// Why one function instead of scattering fetch() calls everywhere:
//   1. The JWT (Authorization header) only needs to be attached in ONE
//      place, not copy-pasted into every page that calls the API.
//   2. Error handling (reading the backend's { message: "..." } shape) is
//      consistent everywhere, instead of every page re-implementing it.
//   3. If the API's base URL or auth mechanism ever changes, there's
//      exactly one file to update.

// In local development, this stays empty and every call is a relative
// path like '/api/auth/login' - Vite's dev server proxies that straight
// to the Spring Boot backend (see vite.config.js), so the browser never
// needs to know the backend's real port.
//
// For a production build (no Vite dev proxy), set VITE_API_BASE_URL to
// the deployed backend's URL, e.g. https://api.hemoconnect.example.com
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

/**
 * Makes an authenticated API call and returns the parsed JSON response.
 * Throws an Error with the backend's message on failure, so callers can
 * just try/catch (or let AuthContext's existing try/catch patterns work
 * unchanged).
 */
export async function apiRequest(path, { method = 'GET', body, skipAuth = false } = {}) {
  const headers = { 'Content-Type': 'application/json' };

  if (!skipAuth) {
    const token = localStorage.getItem('token');
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  // 204 No Content (e.g. DELETE, mark-all-as-read) has no body to parse.
  if (response.status === 204) {
    return null;
  }

  const data = await response.json();

  if (!response.ok) {
    // Matches GlobalExceptionHandler's ErrorResponse shape on the backend:
    // { timestamp, status, error, message, fieldErrors? }
    const error = new Error(data.message || 'Something went wrong');
    error.status = response.status;
    error.fieldErrors = data.fieldErrors;
    throw error;
  }

  return data;
}
