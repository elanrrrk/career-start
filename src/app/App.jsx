import { setAccessTokenGetter } from "@/shared/api";
import { useSessionStore } from "@/entities/session";
import { AppRouter } from "./router";

// HTTP-клиент берёт access-токен из сессии для заголовка Authorization
setAccessTokenGetter(() => useSessionStore.getState().accessToken);

// Сюда позже добавятся провайдеры: QueryClientProvider и т.п.
export function App() {
  return <AppRouter />;
}
