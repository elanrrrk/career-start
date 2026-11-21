export const API_URL = import.meta.env.VITE_API_URL ?? "/api";

// Пока бэкенда нет, запросы подменяются моками.
// Когда бэкенд появится, поставьте VITE_USE_MOCK_API=false в .env.development
export const USE_MOCK_API = import.meta.env.VITE_USE_MOCK_API !== "false";
