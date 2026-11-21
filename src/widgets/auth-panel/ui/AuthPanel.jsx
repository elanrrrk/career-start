import { Section, Title } from "@/shared/ui";
import s from "./AuthPanel.module.css";

// Общая раскладка экранов входа и регистрации
export function AuthPanel({ title, subtitle, social, footer, children }) {
  return (
    <Section tone="auth" fullHeight contentClassName={s.content}>
      <Title as="h1" size="md" className={s.title}>
        {title}
      </Title>
      {subtitle && <p className={s.subtitle}>{subtitle}</p>}
      {social && <div className={s.social}>{social}</div>}
      <div className={s.body}>{children}</div>
      {footer && <div className={s.footer}>{footer}</div>}
    </Section>
  );
}
