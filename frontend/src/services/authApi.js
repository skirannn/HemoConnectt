// Module 9: thin wrappers around every real /api/auth/** endpoint built
// in Module 2. Nothing in here is business logic - it's purely "what URL,
// what method, what body" for each call, matching the backend exactly
// (see docs/modules/auth.md for the backend side of each of these).
import { apiRequest } from './httpClient';

export function login(email, password, rememberMe) {
  return apiRequest('/api/auth/login', {
    method: 'POST',
    body: { email, password, rememberMe },
    skipAuth: true, // you don't have a token yet - that's the whole point of logging in
  });
}

export function signup(userData) {
  // userData is the raw signup form (name, email, password, bloodGroup,
  // location, role, ...). The backend ignores fields it doesn't
  // recognize (see application.yml's fail-on-unknown-properties: false),
  // so it's fine that the form also includes `confirmPassword`.
  return apiRequest('/api/auth/signup', {
    method: 'POST',
    body: userData,
    skipAuth: true,
  });
}

export function verifyToken() {
  return apiRequest('/api/auth/verify');
}

export function sendOtp(email) {
  return apiRequest('/api/auth/send-otp', {
    method: 'POST',
    body: { email },
    skipAuth: true,
  });
}

export function resetPassword(email, otp, newPassword) {
  return apiRequest('/api/auth/reset-password', {
    method: 'POST',
    body: { email, otp, newPassword },
    skipAuth: true,
  });
}
