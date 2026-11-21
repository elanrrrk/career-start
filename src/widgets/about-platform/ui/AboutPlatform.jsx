import { Section } from "@/shared/ui";
import s from "./AboutPlatform.module.css";

export function AboutPlatform({ id }) {
  return (
    <Section id={id} tone="dark" fullHeight className={s.section}>
      <div className={s.content}>
        <p className={s.lead}>
          <span className={s.brand}>Карьер Старт —</span>
          <br />
          это платформа, которая дает шанс получить оффер мечты в начале пути.
        </p>
        <p className={s.text}>Решай кейсы от топ-компаний, соревнуйся и становись крутым специалистом.</p>
      </div>
      <ArcsDecor />
    </Section>
  );
}

function ArcsDecor() {
  return (
    <svg className={s.arcs} viewBox="0 0 600 600" fill="none" aria-hidden="true">
      <defs>
        <linearGradient id="about-arc" x1="520" y1="0" x2="80" y2="600" gradientUnits="userSpaceOnUse">
          <stop offset="0" stopColor="#fff" stopOpacity="0" />
          <stop offset="0.25" stopColor="#efe6ff" />
          <stop offset="0.6" stopColor="#8b5cf6" />
          <stop offset="1" stopColor="#8b5cf6" stopOpacity="0" />
        </linearGradient>
        <filter id="about-arc-glow" x="-20%" y="-20%" width="140%" height="140%">
          <feGaussianBlur stdDeviation="8" />
        </filter>
      </defs>
      <path d="M470 0C430 260 300 460 40 580" stroke="url(#about-arc)" strokeWidth="14" filter="url(#about-arc-glow)" opacity="0.55" />
      <path d="M470 0C430 260 300 460 40 580" stroke="url(#about-arc)" strokeWidth="2" />
      <path d="M600 160C560 340 450 480 240 590" stroke="url(#about-arc)" strokeWidth="12" filter="url(#about-arc-glow)" opacity="0.45" />
      <path d="M600 160C560 340 450 480 240 590" stroke="url(#about-arc)" strokeWidth="1.5" />
    </svg>
  );
}
