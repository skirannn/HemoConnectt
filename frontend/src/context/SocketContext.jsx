import { createContext, useContext, useState } from 'react';

// Module 9: the original app pushed live notifications over Socket.IO.
// The project brief explicitly deferred real-time delivery (see Module 6
// - "Real-time WebSocket functionality can be added later if required"),
// and this backend doesn't run a Socket.IO server. Rather than have the
// app try to connect to a socket server that doesn't exist (and spam the
// console with failed reconnect attempts), this is a no-op stand-in that
// keeps the same `useSocket()` shape every component already expects.
//
// The real functionality it's replacing - "see new notifications without
// reloading the page" - is available today via polling:
// GET /api/notifications/unread-count (see docs/modules/notifications.md).
// A future enhancement could restore this file's real Socket.IO
// connection once the backend adds a WebSocket endpoint.
const SocketContext = createContext();

export function SocketProvider({ children }) {
  const [notifications, setNotifications] = useState([]);

  const value = {
    socket: null,
    notifications,
    setNotifications,
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
    throw new Error('useSocket must be used within a SocketProvider');
  }
  return context;
};
