import { Section, Title } from "@/shared/ui";
import { ModeTabs } from "@/features/landing-mode";
import s from "./Hero.module.css";

export function Hero({ mode, onModeChange, contentId }) {
  return (
    <Section tone="hero" fullHeight contentClassName={s.inner}>
      <Title as="h1" size="xl" className={s.title}>
        Твоя карьера
        <br />
        начинается здесь
      </Title>
      <ModeTabs value={mode} onChange={onModeChange} controls={contentId} className={s.tabs} />
    </Section>
  );
}
