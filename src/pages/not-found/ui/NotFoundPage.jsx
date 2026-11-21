import { Link } from "react-router";
import { Section, Title } from "@/shared/ui";
import { ROUTES } from "@/shared/config/routes";
import s from "./NotFoundPage.module.css";

export function NotFoundPage() {
  return (
    <Section tone="dark" fullHeight>
      <Title size="lg">Эта страница пока в разработке</Title>
      <Link to={ROUTES.home} className={s.link}>
        Вернуться на главную
      </Link>
    </Section>
  );
}
