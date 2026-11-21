import { Link, NavLink } from "react-router";
import { UserRound } from "lucide-react";
import { Container } from "@/shared/ui";
import { ROUTES } from "@/shared/config/routes";
import { cn } from "@/shared/lib/cn";
import { selectIsAuthorized, useSessionStore } from "@/entities/session";
import s from "./Header.module.css";

const NAV_ITEMS = [
  { label: "О нас", to: ROUTES.about },
  { label: "Блог", to: ROUTES.blog },
  { label: "Направления", to: ROUTES.directions },
  { label: "Партнеры", to: ROUTES.partners },
];

const linkClass = ({ isActive }) => cn(s.link, isActive && s.active);

/** theme: "dark" — тёмная плашка и белый текст, "light" — светлая плашка и чёрный текст */
export function Header({ theme = "dark" }) {
  const isAuthorized = useSessionStore(selectIsAuthorized);

  return (
    <header className={cn(s.header, theme === "light" && s.light)}>
      <Container>
        <nav className={s.bar} aria-label="Основная навигация">
          <ul className={s.links}>
            {NAV_ITEMS.map((item) => (
              <li key={item.to}>
                <NavLink to={item.to} className={linkClass}>
                  {item.label}
                </NavLink>
              </li>
            ))}
          </ul>

          <div className={s.right}>
            <NavLink to={ROUTES.companies} className={linkClass}>
              Компаниям
            </NavLink>
            <Link
              to={isAuthorized ? ROUTES.profile : ROUTES.login}
              className={s.profile}
              aria-label={isAuthorized ? "Профиль" : "Войти"}
            >
              <UserRound size={22} strokeWidth={1.75} />
            </Link>
          </div>
        </nav>
      </Container>
    </header>
  );
}
