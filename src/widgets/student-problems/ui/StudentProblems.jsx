import { CircleCheck, CircleX } from "lucide-react";
import { Section, Title } from "@/shared/ui";
import { PROBLEMS, SOLUTIONS } from "../model/content";
import s from "./StudentProblems.module.css";

export function StudentProblems({ id }) {
  return (
    <Section id={id} tone="dark" fullHeight>
      <Title size="md" accentLine>
        Решаем главные проблемы студентов
      </Title>

      <div className={s.columns}>
        <ItemList items={PROBLEMS} icon={<CircleX className={s.iconProblem} />} />
        <ItemList items={SOLUTIONS} icon={<CircleCheck className={s.iconSolution} />} />
      </div>
    </Section>
  );
}

function ItemList({ items, icon }) {
  return (
    <ul className={s.list}>
      {items.map((text) => (
        <li key={text} className={s.item}>
          <span className={s.icon} aria-hidden="true">
            {icon}
          </span>
          <span className={s.card}>{text}</span>
        </li>
      ))}
    </ul>
  );
}
