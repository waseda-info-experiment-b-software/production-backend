"use client";

import { useWebsocket } from "@/hooks/useWebsocket";
// import { WebSocketTest } from "@/features/WebSocketTest";
import { FileTableView } from "features/FileTableView";

export default function Home() {
  const { sendMessage } = useWebsocket("ws://localhost:8080");
  return (
    <main className="max-w-[768px] mx-auto">
      <div className="py-6">
        <div className="flex items-center justify-between py-6">
          <div className="px-3 py-1 border border-solid border-gray-400 bg-gray-600 rounded-md">
            develop
          </div>
          <button
            onClick={() => sendMessage("a")}
            className="px-3 py-1 rounded-md border border-solid border-green-950 bg-green-600"
          >
            Code
          </button>
        </div>
        <FileTableView />
      </div>
      {/* <WebSocketTest /> */}
    </main>
  );
}
