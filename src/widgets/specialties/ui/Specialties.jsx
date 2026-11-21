import { ArrowRight } from "lucide-react";
import { Section, Title } from "@/shared/ui";
import { MOCK_PROFESSIONS, ProfessionCard } from "@/entities/profession";
import { SuggestProfessionForm } from "@/features/suggest-profession";
import s from "./Specialties.module.css";

export function Specialties({ id, onStartTest }) {
  // TODO: заменить мок на запрос к API (TanStack Query: useProfessions())
  const professions = MOCK_PROFESSIONS;

  return (
    <Section id={id} tone="dark-glow" fullHeight>
      <div className={s.content}>
        <Title size="lg" className={s.title}>
          Специальности
        </Title>

        <ul className={s.list}>
          {professions.map((profession) => (
            <li key={profession.id}>
              <ProfessionCard profession={profession} />
            </li>
          ))}
        </ul>

        <SuggestProfessionForm />

        <button type="button" className={s.testButton} onClick={onStartTest}>
          Пройти тест на профориентацию
          <ArrowRight size={22} strokeWidth={1.5} aria-hidden="true" />
        </button>
      </div>
    </Section>
  );
}
