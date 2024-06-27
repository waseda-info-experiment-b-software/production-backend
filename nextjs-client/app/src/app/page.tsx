"use client";

import Image from "next/image";
import { useEffect, useState } from "react";

export default function Home() {
  const [data, setData] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      const response = await fetch("http://localhost:8082/api/data"); // Javaアプリケーションのエンドポイント
      const textData = await response.text();
      setData(textData);
    };

    fetchData();
  }, []);

  return (
    <div>
      <h1>Data from Java Server:</h1>
      <p>{data}</p>
    </div>
  );
}
