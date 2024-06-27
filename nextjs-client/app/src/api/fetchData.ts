async function fetchData() {
  const response = await fetch("http://localhost:8082/api/data");

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return await response.text();
}
