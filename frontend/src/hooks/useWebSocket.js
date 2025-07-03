import { useEffect, useRef, useState } from "react";

/**
 * useWebSocket
 * Custom React hook for managing a WebSocket connection.
 * Garante que o callback 'onMessage' está sempre atualizado!
 */
export default function useWebSocket(endpoint, token, onMessage) {
  const ws = useRef(null);
  const [isConnected, setIsConnected] = useState(false);

  // Ref para garantir que o onMessage é sempre o mais recente
  const onMessageRef = useRef(onMessage);
  useEffect(() => { onMessageRef.current = onMessage }, [onMessage]);

  useEffect(() => {
    if (!token) return; // Only connect if token is available

    const url = `${endpoint}?token=${token}`;
    ws.current = new WebSocket(url);

    ws.current.onopen = () =>  {
      setIsConnected(true);
      // PING interval para manter ligação aberta
      ws.current.pingInterval = setInterval(() => {
        if (ws.current?.readyState === WebSocket.OPEN) {
          ws.current.send(JSON.stringify({ type: "ping" }));
        }
      }, 25000);
    };

    ws.current.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data?.type === "ping") return;
        // Chama sempre o callback mais recente
        onMessageRef.current?.(data);
      } catch {
        // Mensagem não é JSON (ex: simples texto de confirmação)
        // podes ignorar ou mostrar, como preferires
      }
    };

    ws.current.onerror = (e) => {
      console.error("WebSocket error", e);
    };

    ws.current.onclose = () => {
      setIsConnected(false);
      if (ws.current.pingInterval) clearInterval(ws.current.pingInterval);
    };

    // CLEANUP on unmount/desconexão
    return () => {
      ws.current?.close();
      if (ws.current?.pingInterval) clearInterval(ws.current.pingInterval);
    };
  }, [endpoint, token]);

  // Função para enviar mensagens
  const sendMessage = (data) => {
    if (ws.current && ws.current.readyState === WebSocket.OPEN) {
      ws.current.send(JSON.stringify(data));
    }
  };

  return { sendMessage, isConnected };
}
