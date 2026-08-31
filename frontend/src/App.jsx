import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from 'react-router-dom';

import { AuthProvider, useAuth } from './context/AuthContext';
import { SocketProvider } from './context/SocketContext';

import Layout from './components/Layout/Layout';
import ProtectedRoute from './components/Auth/ProtectedRoute';

import HomePage from './pages/Home/HomePage';
import LoginPage from './pages/Auth/LoginPage';
import SignupPage from './pages/Auth/SignupPage';
import ForgotPasswordPage from './pages/Auth/ForgotPasswordPage';

import ProfileSetupPage from './pages/Auth/ProfileSetupPage';
import ProfilePage from './pages/Auth/ProfilePage';

import DonorDashboard from './pages/Dashboard/DonorDashboard';
import RecipientDashboard from './pages/Dashboard/RecipientDashboard';
import AdminDashboard from './pages/Dashboard/AdminDashboard';

import BloodRequestPage from './pages/Request/BloodRequestPage';
import DonorMatchingPage from './pages/Matching/DonorMatchingPage';

import NotificationsPage from './pages/Notifications/NotificationsPage';

import DonorHistoryPage from './pages/History/DonorHistoryPage';
import RecipientHistoryPage from './pages/History/RecipientHistoryPage';

import AboutPage from './pages/About/AboutPage';
import ContactPage from './pages/Contact/ContactPage';

import NotFoundPage from './pages/Error/NotFoundPage';
import UnauthorizedPage from './pages/Error/UnauthorizedPage';
import ServerErrorPage from './pages/Error/ServerErrorPage';
import NoDonorsPage from './pages/Error/NoDonorsPage';


/*
 * /dashboard is the common dashboard URL.
 *
 * Instead of always opening DonorDashboard,
 * we check the logged-in user's role and redirect
 * to the correct dashboard.
 */
function DashboardRedirect() {
  const { user } = useAuth();

  if (user?.role === 'donor') {
    return (
      <Navigate
        to="/donor-dashboard"
        replace
      />
    );
  }

  if (user?.role === 'recipient') {
    return (
      <Navigate
        to="/recipient-dashboard"
        replace
      />
    );
  }

  if (user?.role === 'admin') {
    return (
      <Navigate
        to="/admin-dashboard"
        replace
      />
    );
  }

  return (
    <Navigate
      to="/"
      replace
    />
  );
}


function App() {
  return (
    <AuthProvider>
      <SocketProvider>
        <Router>
          <Layout>

            <Routes>

              {/* =========================
                  PUBLIC ROUTES
                 ========================= */}

              <Route
                path="/"
                element={<HomePage />}
              />

              <Route
                path="/login"
                element={<LoginPage />}
              />

              <Route
                path="/signup"
                element={<SignupPage />}
              />

              <Route
                path="/forgot-password"
                element={<ForgotPasswordPage />}
              />

              <Route
                path="/about"
                element={<AboutPage />}
              />

              <Route
                path="/contact"
                element={<ContactPage />}
              />


              {/* =========================
                  PROFILE ROUTES
                 ========================= */}

              <Route
                path="/profile-setup"
                element={
                  <ProtectedRoute>
                    <ProfileSetupPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/profile"
                element={
                  <ProtectedRoute>
                    <ProfilePage />
                  </ProtectedRoute>
                }
              />


              {/* =========================
                  DASHBOARD ROUTES
                 ========================= */}

              {/* Common dashboard URL */}
              <Route
                path="/dashboard"
                element={
                  <ProtectedRoute>
                    <DashboardRedirect />
                  </ProtectedRoute>
                }
              />


              {/* Donor Dashboard */}
              <Route
                path="/donor-dashboard"
                element={
                  <ProtectedRoute requiredRole="donor">
                    <DonorDashboard />
                  </ProtectedRoute>
                }
              />


              {/* Recipient Dashboard */}
              <Route
                path="/recipient-dashboard"
                element={
                  <ProtectedRoute requiredRole="recipient">
                    <RecipientDashboard />
                  </ProtectedRoute>
                }
              />


              {/* Admin Dashboard */}
              <Route
                path="/admin-dashboard"
                element={
                  <ProtectedRoute requiredRole="admin">
                    <AdminDashboard />
                  </ProtectedRoute>
                }
              />


              {/* =========================
                  FEATURE ROUTES
                 ========================= */}

              <Route
                path="/request-blood"
                element={
                  <ProtectedRoute>
                    <BloodRequestPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/find-donors"
                element={
                  <ProtectedRoute>
                    <DonorMatchingPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/notifications"
                element={
                  <ProtectedRoute>
                    <NotificationsPage />
                  </ProtectedRoute>
                }
              />


              {/* =========================
                  HISTORY ROUTES
                 ========================= */}

              <Route
                path="/donor-history"
                element={
                  <ProtectedRoute requiredRole="donor">
                    <DonorHistoryPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/recipient-history"
                element={
                  <ProtectedRoute requiredRole="recipient">
                    <RecipientHistoryPage />
                  </ProtectedRoute>
                }
              />


              {/* =========================
                  ERROR ROUTES
                 ========================= */}

              <Route
                path="/unauthorized"
                element={<UnauthorizedPage />}
              />

              <Route
                path="/server-error"
                element={<ServerErrorPage />}
              />

              <Route
                path="/no-donors"
                element={<NoDonorsPage />}
              />


              {/* =========================
                  404
                 ========================= */}

              <Route
                path="*"
                element={<NotFoundPage />}
              />

            </Routes>

          </Layout>
        </Router>
      </SocketProvider>
    </AuthProvider>
  );
}

export default App;