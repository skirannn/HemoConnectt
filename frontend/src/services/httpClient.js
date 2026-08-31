
// Centralized API client for React <-> Spring Boot.

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export async function apiRequest(
  path,
  { method = 'GET', body, skipAuth = false } = {}
) {
  const headers = {
    'Content-Type': 'application/json',
  };

  if (!skipAuth) {
    const token = localStorage.getItem('token');

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body:
      body !== undefined
        ? JSON.stringify(body)
        : undefined,
  });

  // 204 = successful request with no response body.
  if (response.status === 204) {
    return null;
  }

  /*
   * Some Spring Security errors such as 401/403 can have
   * an empty response body.
   *
   * Therefore we MUST NOT blindly call response.json().
   */
  const contentType =
    response.headers.get('content-type') || '';

  let data = null;

  if (contentType.includes('application/json')) {
    const text = await response.text();

    if (text.trim()) {
      try {
        data = JSON.parse(text);
      } catch (error) {
        console.error(
          'Failed to parse API JSON response:',
          error
        );
      }
    }
  } else {
    const text = await response.text();

    if (text.trim()) {
      data = {
        message: text,
      };
    }
  }

  if (!response.ok) {
    let message = 'Something went wrong';

    if (data?.message) {
      message = data.message;
    } else if (response.status === 401) {
      message = 'Unauthorized. Please login again.';
    } else if (response.status === 403) {
      message =
        'Access denied. Your account does not have permission for this action.';
    } else if (response.status === 404) {
      message = 'Requested resource was not found.';
    } else if (response.status >= 500) {
      message =
        'Server error. Please try again later.';
    }

    const error = new Error(message);

    error.status = response.status;
    error.fieldErrors = data?.fieldErrors;

    throw error;
  }

  return data;
}