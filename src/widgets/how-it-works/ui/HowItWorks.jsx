import { Section, Title } from "@/shared/ui";
import { ECOSYSTEM_STEPS } from "../model/steps";
import s from "./HowItWorks.module.css";

export function HowItWorks({ id }) {
  return (
    <Section id={id} tone="dark-glow" fullHeight>
      <Title size="lg" uppercase>
        Как это работает?
      </Title>
      <p className={s.intro}>
        Мы разработали комплексный подход для подготовки и развития карьеры перспективных соискателей
      </p>
      <div className={s.divider} aria-hidden="true" />

      <h3 className={s.subtitle}>Ключевые элементы экосистемы</h3>

      <ol className={s.timeline}>
        {ECOSYSTEM_STEPS.map(({ icon: Icon, color, title, text }) => (
          <li key={title} className={s.step}>
            <span className={s.iconBox} style={{ color }}>
              <Icon size={30} strokeWidth={1.5} aria-hidden="true" />
            </span>
            <h4 className={s.stepTitle}>{title}</h4>
            <p className={s.stepText}>{text}</p>
          </li>
        ))}
      </ol>
    </Section>
  );
}
