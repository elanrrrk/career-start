import { createBrowserRouter } from "react-router";
import { RouterProvider } from "react-router/dom";
import { RootLayout } from "./layouts/RootLayout";
import { HomePage } from "@/pages/home";
import { LoginPage } from "@/pages/login";
import { RegisterPage } from "@/pages/register";
import { NotFoundPage } from "@/pages/not-found";
import { ROUTES } from "@/shared/config/routes";

// handle.headerTheme — вид хедера на странице: "dark" (по умолчанию) или "light"
const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
      { path: ROUTES.home, element: <HomePage /> },
      { path: ROUTES.login, element: <LoginPage />, handle: { headerTheme: "light" } },
      { path: ROUTES.register, element: <RegisterPage />, handle: { headerTheme: "light" } },
      { path: "*", element: <NotFoundPage /> },
    ],
  },
]);

export function AppRouter() {
  return <RouterProvider router={router} />;
}
