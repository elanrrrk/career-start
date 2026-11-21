import { useId } from "react";
import { cn } from "@/shared/lib/cn";
import s from "./Input.module.css";

/**
 * Текстовое поле с сообщением об ошибке.
 * endAdornment — элемент справа внутри поля (например, кнопка «показать пароль»).
 * ref передаётся как обычный проп (React 19), поэтому работает с react-hook-form.
 */
export function Input({ error, endAdornment, className, id, ...rest }) {
  const autoId = useId();
  const inputId = id ?? autoId;
  const errorId = `${inputId}-error`;

  return (
    <div className={cn(s.field, className)}>
      <div className={s.control}>
        <input
          id={inputId}
          className={cn(s.input, endAdornment && s.withAdornment, error && s.invalid)}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? errorId : undefined}
          {...rest}
        />
        {endAdornment && <div className={s.adornment}>{endAdornment}</div>}
      </div>
      {error && (
        <p id={errorId} className={s.error}>
          {error}
        </p>
      )}
    </div>
  );
}
