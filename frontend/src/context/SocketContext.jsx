
import {
  createContext,
  useContext,
  useState,
  useEffect,
} from 'react';

import { useAuth } from './AuthContext';

const SocketContext = createContext();

export function SocketProvider({ children }) {
  const { user, isAuthenticated } = useAuth();

  const [notifications, setNotifications] = useState([]);

  const fetchNotifications = async () => {
    if (!isAuthenticated || !user) {
      setNotifications([]);
      return;
    }

    const token = localStorage.getItem('token');

    if (!token) {
      setNotifications([]);
      return;
    }

    try {
      const response = await fetch(
        '/api/notifications',
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        console.error(
          'Failed to fetch notifications:',
          response.status
        );

        return;
      }

      const data = await response.json();

      setNotifications(
        Array.isArray(data) ? data : []
      );
    } catch (error) {
      console.error(
        'Failed to fetch notifications:',
        error
      );
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, [isAuthenticated, user]);

  /*
   * Poll every 10 seconds so notifications created by
   * another user appear without manually refreshing.
   */
  useEffect(() => {
    if (!isAuthenticated || !user) {
      return;
    }

    const interval = setInterval(() => {
      fetchNotifications();
    }, 10000);

    return () => {
      clearInterval(interval);
    };
  }, [isAuthenticated, user]);

  const value = {
    socket: null,
    notifications,
    setNotifications,
    refreshNotifications: fetchNotifications,
  };

  return (
    <SocketContext.Provider value={value}>
      {children}
    </SocketContext.Provider>
  );
}

export const useSocket = () => {
  const context = useContext(SocketContext);

  if (!context) {
    throw new Error(
      'useSocket must be used within a SocketProvider'
    );
  }

  return context;
};