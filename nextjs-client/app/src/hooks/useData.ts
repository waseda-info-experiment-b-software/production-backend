"use client";

import { useEffect, useState } from "react";

export const useData = () => {
  const [data, setData] = useState<string[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchData = async () => {
      const response = await fetch("http://localhost:8082/api/data");
      const textData = (await response.text()).split("\n");
      setData(textData);
      setLoading(false);
    };

    fetchData();
  }, []);

  return { data, loading };
};
