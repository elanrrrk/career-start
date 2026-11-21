import { create } from "zustand";

// Сессия хранится только в памяти: access-токен не кладём в localStorage.
// После перезагрузки страницы сессию восстановит запрос /auth/refresh (TODO).
export const useSessionStore = create((set) => ({
  user: null,
  accessToken: null,
  setSession: ({ user, accessToken }) => set({ user, accessToken }),
  clearSession: () => set({ user: null, accessToken: null }),
}));

export const selectIsAuthorized = (state) => Boolean(state.user);
