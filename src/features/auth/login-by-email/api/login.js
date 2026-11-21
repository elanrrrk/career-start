import { api } from "@/shared/api";
import { USE_MOCK_API } from "@/shared/config/env";
import { mockResponse } from "@/shared/lib/mock";

/**
 * POST /auth/login
 * body:     { email, password }
 * response: { user: { id, firstName, lastName, email }, accessToken }
 */
export function login({ email, password }) {
  if (USE_MOCK_API) {
    return mockResponse({
      user: { id: 1, firstName: "Тестовый", lastName: "Пользователь", email },
      accessToken: "mock-access-token",
    });
  }
  return api.post("/auth/login", { email, password });
}
