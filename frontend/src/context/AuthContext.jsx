import { createContext, useContext, useReducer, useEffect } from 'react';
import * as authApi from '../services/authApi';
import { normalizeUser } from '../services/mappers';

const AuthContext = createContext();

const initialState = {
  user: null,
  isAuthenticated: false,
  isLoading: true,
};

function authReducer(state, action) {
  switch (action.type) {
    case 'LOGIN_SUCCESS':
      return {
        ...state,
        user: action.payload,
        isAuthenticated: true,
        isLoading: false,
      };
    case 'LOGOUT':
      return {
        ...state,
        user: null,
        isAuthenticated: false,
        isLoading: false,
      };
    case 'SET_LOADING':
      return {
        ...state,
        isLoading: action.payload,
      };
    case 'UPDATE_USER':
      return {
        ...state,
        user: { ...state.user, ...action.payload },
      };
    default:
      return state;
  }
}

// Module 9: this file is the ONE place that talks to authApi.js (the
// real Spring Boot backend from Modules 1-2) and normalizes what comes
// back into the shape the rest of the app already expects. Every page
// below this (LoginPage, SignupPage, ProtectedRoute, ...) is completely
// unaware that the backend changed - they just call useAuth() the same
// way they always did.
export function AuthProvider({ children }) {
  const [state, dispatch] = useReducer(authReducer, initialState);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      restoreSession();
    } else {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const restoreSession = async () => {
    try {
      const backendUser = await authApi.verifyToken();
      dispatch({ type: 'LOGIN_SUCCESS', payload: normalizeUser(backendUser) });
    } catch (error) {
      // Expired/invalid token - GlobalExceptionHandler on the backend
      // already turned this into a clean 401/403 before it got here.
      console.error('Session restore failed:', error);
      localStorage.removeItem('token');
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  };

  const login = async (email, password, rememberMe = false) => {
    try {
      const data = await authApi.login(email, password, rememberMe);
      localStorage.setItem('token', data.token);
      const user = normalizeUser(data.user);
      dispatch({ type: 'LOGIN_SUCCESS', payload: user });
      return { success: true, user };
    } catch (error) {
      return { success: false, message: error.message || 'Network error occurred' };
    }
  };

  const signup = async (userData) => {
    try {
      const data = await authApi.signup(userData);
      localStorage.setItem('token', data.token);
      const user = normalizeUser(data.user);
      dispatch({ type: 'LOGIN_SUCCESS', payload: user });
      return { success: true, user };
    } catch (error) {
      return { success: false, message: error.message || 'Network error occurred' };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    dispatch({ type: 'LOGOUT' });
  };

  const updateUser = (userData) => {
    dispatch({ type: 'UPDATE_USER', payload: userData });
  };

  const value = {
    ...state,
    login,
    signup,
    logout,
    updateUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
