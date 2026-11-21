import { Button, Title } from "@/shared/ui";
import s from "./CareerTest.module.css";

export function TestIntro({ onStart, onSkip }) {
  return (
    <div className={s.intro}>
      <Title size="lg" uppercase>
        Найди своё призвание
      </Title>
      <p className={s.introText}>
        Пройди тест на профориентацию, чтобы понять, какая карьера подходит именно тебе
      </p>
      <Button size="lg" uppercase className={s.startButton} onClick={onStart}>
        Начать
      </Button>
      <Button variant="link" uppercase className={s.skip} onClick={onSkip}>
        Пропустить тестирование
      </Button>
    </div>
  );
}
