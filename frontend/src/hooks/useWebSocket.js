import { useEffect, useRef, useState } from "react";

/**
 * useWebSocket
 * Custom React hook for managing a WebSocket connection.
 *
 * @param {string} endpoint - The WebSocket endpoint (without token).
 * @param {string} token - The authentication token to append as a query param.
 * @param {function} onMessage - Callback to handle incoming messages (parsed as JSON).
 * @returns {object} { sendMessage, isConnected }
 */
export default function useWebSocket(endpoint, token, onMessage) {
  const ws = useRef(null);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    if (!token) return; // Only connect if token is available

    const url = `${endpoint}?token=${token}`;
    ws.current = new WebSocket(url);

    ws.current.onopen = () => setIsConnected(true);

    ws.current.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        onMessage?.(data);
      } catch (e) {
        console.warn("Invalid WebSocket message", event.data);
      }
    };

    ws.current.onerror = (e) => {
      console.error("WebSocket error", e);
    };

    ws.current.onclose = () => setIsConnected(false);

    return () => {
      ws.current?.close();
    };
    // Only reconnect if the token changes
  }, [endpoint, token]);

  // Function to send messages over the WebSocket
  const sendMessage = (data) => {
    if (ws.current && ws.current.readyState === WebSocket.OPEN) {
      ws.current.send(JSON.stringify(data));
    }
  };

  return { sendMessage, isConnected };
}
