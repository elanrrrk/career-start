import { useState } from "react";
import { Plus } from "lucide-react";
import s from "./SuggestProfessionForm.module.css";

export function SuggestProfessionForm() {
  const [value, setValue] = useState("");
  const [isSent, setIsSent] = useState(false);

  const handleSubmit = (event) => {
    event.preventDefault();
    const title = value.trim();
    if (!title) return;

    // TODO: отправить на бэкенд, например api.post("/professions/suggestions", { title })
    setIsSent(true);
    setValue("");
  };

  return (
    <form className={s.form} onSubmit={handleSubmit}>
      <div className={s.field}>
        <input
          className={s.input}
          value={value}
          onChange={(e) => {
            setValue(e.target.value);
            setIsSent(false);
          }}
          placeholder="Предложи свою профессию"
          aria-label="Название профессии"
        />
        <button type="submit" className={s.submit} aria-label="Отправить предложение">
          <Plus size={22} strokeWidth={1.5} />
        </button>
      </div>
      <p className={s.status} aria-live="polite">
        {isSent && "Предложение отправлено. Спасибо!"}
      </p>
    </form>
  );
}
