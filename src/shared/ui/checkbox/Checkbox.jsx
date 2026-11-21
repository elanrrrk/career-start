import { useId } from "react";
import { cn } from "@/shared/lib/cn";
import s from "./Checkbox.module.css";

export function Checkbox({ error, className, children, id, ...rest }) {
  const autoId = useId();
  const inputId = id ?? autoId;
  const errorId = `${inputId}-error`;

  return (
    <div className={cn(s.field, className)}>
      <label className={s.label} htmlFor={inputId}>
        <input
          id={inputId}
          type="checkbox"
          className={s.box}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? errorId : undefined}
          {...rest}
        />
        <span>{children}</span>
      </label>
      {error && (
        <p id={errorId} className={s.error}>
          {error}
        </p>
      )}
    </div>
  );
}
