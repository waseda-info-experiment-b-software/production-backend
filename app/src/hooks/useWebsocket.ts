import { useEffect, useState } from "react";

export const useWebsocket = (url: string) => {
  const [socket, setSocket] = useState<WebSocket | null>(null);
  const [messages, setMessages] = useState<string[]>([]);

  useEffect(() => {
    const ws = new WebSocket(url);

    ws.onopen = () => {
      console.log("WebSocket connection opened");
      setSocket(ws);
    };

    ws.onmessage = (event) => {
      console.log("Message received from server:", event.data);
      setMessages((prevMessages) => [...prevMessages, event.data]);
    };

    ws.onclose = (e) => {
      console.log("WebSocket connection closed:" + e.code + " " + e.reason);
      setSocket(null);
    };

    ws.onerror = (error) => {
      console.error("WebSocket error:", error);
    };

    return () => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.close();
      }
    };
  }, [url]);

  const sendMessage = (message: string) => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(message);
      console.log("Sent message:", message);
    } else {
      console.error("WebSocket is not open. readyState:", socket?.readyState);
    }
  };

  return { messages, sendMessage };
};
