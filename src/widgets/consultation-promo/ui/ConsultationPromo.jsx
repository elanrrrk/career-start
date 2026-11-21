import { Button, Section, Title } from "@/shared/ui";
import s from "./ConsultationPromo.module.css";

export function ConsultationPromo({ id }) {
  return (
    <Section id={id} tone="glow" fullHeight>
      <div className={s.content}>
        <Title size="md">Консультация со специалистом-профориентологом</Title>
        <p className={s.text}>
          Получите персональные рекомендации и определите карьерное направление с экспертом
        </p>
        {/* TODO: открыть форму записи (features/book-consultation) */}
        <Button className={s.button}>Записаться на консультацию</Button>
      </div>
    </Section>
  );
}
