import { cn } from "@/shared/lib/cn";
import { Container } from "../container/Container";
import s from "./Section.module.css";

const TONES = {
  hero: s.toneHero,
  glow: s.toneGlow,
  dark: s.toneDark,
  "dark-glow": s.toneDarkGlow,
  auth: s.toneAuth,
};

/**
 * Секция лендинга с фоном из дизайн-системы.
 * tone: "hero" | "glow" (светло-фиолетовый) | "dark" | "dark-glow" | "auth"
 */
export function Section({ id, tone = "dark", fullHeight = false, className, contentClassName, children }) {
  return (
    <section id={id} className={cn(s.section, TONES[tone], fullHeight && s.fullHeight, className)}>
      <Container className={cn(s.content, contentClassName)}>{children}</Container>
    </section>
  );
}
