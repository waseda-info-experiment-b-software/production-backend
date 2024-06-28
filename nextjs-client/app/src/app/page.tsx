import { FileTableView } from "features/TableView";

export default function Home() {
  return (
    <main className="max-w-[768px] mx-auto">
      <div className="py-6">
        <div className="flex items-center justify-between py-6">
          <div className="px-3 py-1 border border-solid border-gray-400 text-white bg-gray-600 rounded-md">
            develop
          </div>
        </div>
        <FileTableView />
      </div>
    </main>
  );
}
