import { Button, Title } from "@/shared/ui";
import { MOCK_TEST_RESULT } from "@/entities/test-result";
import s from "./CareerTest.module.css";

export function TestResult({ onShowDirections, onAskQuestion }) {
  return (
    <div className={s.result}>
      <Title size="md">Результат теста:</Title>
      <p className={s.resultText}>{MOCK_TEST_RESULT.summary}</p>
      <div className={s.actions}>
        <Button onClick={onShowDirections}>Смотреть направления</Button>
        <Button variant="outline" onClick={onAskQuestion}>
          Остались вопросы
        </Button>
      </div>
    </div>
  );
}
