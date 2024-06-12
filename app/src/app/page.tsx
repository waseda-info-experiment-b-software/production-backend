import { FileTableView } from "features/FileTableView";
import Image from "next/image";

export default function Home() {
  return (
    <main className="max-w-[768px] mx-auto">
      <div className="py-6">
        <div className="flex items-center justify-between py-6">
          <div className="px-3 py-1 border border-solid border-gray-400 bg-gray-600 rounded-md">
            develop
          </div>
          <button className="px-3 py-1 rounded-md border border-solid border-green-950 bg-green-700">
            Code
          </button>
        </div>
        <FileTableView />
      </div>
    </main>
  );
}
