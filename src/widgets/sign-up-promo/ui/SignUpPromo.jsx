import { Section, Title } from "@/shared/ui";
import s from "./SignUpPromo.module.css";

export function SignUpPromo({ id }) {
  return (
    <Section id={id} tone="glow" fullHeight>
      <Title size="lg" className={s.title}>
        Старт карьеры — в один клик
      </Title>
      <p className={s.text}>
        Зарегистрируйтесь, чтобы получить доступ к реальным кейсам, фаст-трекам и предложениям от
        компаний-партнёров.
      </p>
    </Section>
  );
}
