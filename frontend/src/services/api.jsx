const getApiBaseUrl = () => {
  const envUrl = import.meta.env.VITE_API_BASE_URL;
  if (envUrl && envUrl.trim() !== "") {
    return envUrl.trim().replace(/\/+$/, "");
  }
  return "http://localhost:8080";
};

export const API_BASE_URL = getApiBaseUrl();
export default API_BASE_URL;
