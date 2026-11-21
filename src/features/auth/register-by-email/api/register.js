import { api } from "@/shared/api";
import { USE_MOCK_API } from "@/shared/config/env";
import { mockResponse } from "@/shared/lib/mock";

/**
 * POST /auth/register
 * body:     { firstName, lastName, email, password }
 * response: { user: { id, firstName, lastName, email }, accessToken }
 */
export function registerUser({ firstName, lastName, email, password }) {
  if (USE_MOCK_API) {
    return mockResponse({
      user: { id: 1, firstName, lastName, email },
      accessToken: "mock-access-token",
    });
  }
  return api.post("/auth/register", { firstName, lastName, email, password });
}
