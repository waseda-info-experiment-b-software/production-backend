"use client";

import { useData } from "@/hooks/useData";

export const FileTableView = () => {
  const { data, loading } = useData();

  return (
    <div className="rounded-lg overflow-hidden border border-solid border-gray-600 divide-y divide-gray-600">
      <div className="p-4 bg-gray-200">
        現在、リモートにおいて指しているファイル名の一覧を表示しています
      </div>
      {loading ? (
        Array.from({ length: 4 }).map((_, index) => (
          <div key={index} className="px-4 py-3 bg-gray-50">
            <div className="rounded-md h-[1.2em] w-[20em] bg-gray-300 animate-pulse" />
          </div>
        ))
      ) : data.length === 0 ? (
        <div className="px-4 py-3 bg-gray-50">リポジトリは空です。</div>
      ) : (
        data.map((line, index) => (
          <div key={index} className="px-4 py-3 bg-gray-50">
            {line}
          </div>
        ))
      )}
    </div>
  );
};
