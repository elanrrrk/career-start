import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { Input } from "../input/Input";
import s from "./PasswordInput.module.css";

export function PasswordInput(props) {
  const [isVisible, setIsVisible] = useState(false);

  return (
    <Input
      {...props}
      type={isVisible ? "text" : "password"}
      endAdornment={
        <button
          type="button"
          className={s.toggle}
          onClick={() => setIsVisible((v) => !v)}
          aria-label={isVisible ? "Скрыть пароль" : "Показать пароль"}
          aria-pressed={isVisible}
        >
          {isVisible ? <Eye size={18} strokeWidth={1.5} /> : <EyeOff size={18} strokeWidth={1.5} />}
        </button>
      }
    />
  );
}
