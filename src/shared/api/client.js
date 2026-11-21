import { API_URL } from "@/shared/config/env";

export class ApiError extends Error {
  constructor(message, status, data) {
    super(message);
    this.status = status;
    this.data = data;
  }
}

// shared не знает про сессию (это слой entities), поэтому токен
// передаётся снаружи: см. app/App.jsx
let getAccessToken = () => null;
export const setAccessTokenGetter = (getter) => {
  getAccessToken = getter;
};

async function request(path, { method = "GET", body, headers, ...rest } = {}) {
  const token = getAccessToken();
  let response;

  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      credentials: "include", // для refresh-токена в httpOnly cookie
      headers: {
        ...(body !== undefined && { "Content-Type": "application/json" }),
        ...(token && { Authorization: `Bearer ${token}` }),
        ...headers,
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
      ...rest,
    });
  } catch {
    throw new ApiError("Не удалось связаться с сервером. Проверьте подключение к интернету.", 0);
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    // TODO: при 401 попробовать обновить токен через /auth/refresh
    throw new ApiError(data?.message ?? "Что-то пошло не так. Попробуйте ещё раз.", response.status, data);
  }

  return data;
}

export const api = {
  get: (path, options) => request(path, { ...options, method: "GET" }),
  post: (path, body, options) => request(path, { ...options, method: "POST", body }),
  put: (path, body, options) => request(path, { ...options, method: "PUT", body }),
  delete: (path, options) => request(path, { ...options, method: "DELETE" }),
};
