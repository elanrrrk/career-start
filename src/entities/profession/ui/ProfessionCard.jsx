import { Link } from "react-router";
import { ROUTES } from "@/shared/config/routes";
import s from "./ProfessionCard.module.css";

export function ProfessionCard({ profession }) {
  return (
    <Link to={ROUTES.profession(profession.slug)} className={s.card}>
      {profession.title}
    </Link>
  );
}
