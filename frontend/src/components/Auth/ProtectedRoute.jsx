import { Navigate, useLocation } from 'react-router-dom';
import { Box, Spinner, Center } from '@chakra-ui/react';
import { useAuth } from '../../context/AuthContext';

function ProtectedRoute({ children, requiredRole = null }) {
  const { isAuthenticated, isLoading, user } = useAuth();
  const location = useLocation();

  // Wait until the saved JWT/session has been checked.
  if (isLoading) {
    return (
      <Center minH="50vh">
        <Spinner size="xl" color="primary.500" />
      </Center>
    );
  }

  // User is not logged in.
  if (!isAuthenticated) {
    return (
      <Navigate
        to="/login"
        state={{ from: location }}
        replace
      />
    );
  }

  // Check role if this route requires a specific role.
  if (requiredRole && user?.role !== requiredRole) {
    return <Navigate to="/unauthorized" replace />;
  }

  /*
   * Profile completion check.
   *
   * The Spring Boot backend returns:
   *     profileCompleted
   *
   * normalizeUser() converts that to the frontend property:
   *     profileComplete
   *
   * Therefore the frontend checks user.profileComplete here.
   */
  if (
    user &&
    user.profileComplete !== true &&
    location.pathname !== '/profile-setup'
  ) {
    return (
      <Navigate
        to="/profile-setup"
        replace
      />
    );
  }

  // Everything is valid — allow access to the protected page.
  return <Box>{children}</Box>;
}

export default ProtectedRoute;