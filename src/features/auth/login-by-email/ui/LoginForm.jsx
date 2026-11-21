"use no memo"; // react-hook-form пока плохо дружит с React Compiler, отключаем его для этого файла

import { useState } from "react";
import { Link } from "react-router";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button, Input, PasswordInput } from "@/shared/ui";
import { ROUTES } from "@/shared/config/routes";
import { useSessionStore } from "@/entities/session";
import { loginSchema } from "../model/schema";
import { login } from "../api/login";
import s from "./LoginForm.module.css";

export function LoginForm({ onSuccess }) {
  const setSession = useSessionStore((state) => state.setSession);
  const [serverError, setServerError] = useState(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  const onSubmit = async (values) => {
    setServerError(null);
    try {
      const session = await login(values);
      setSession(session);
      onSuccess?.();
    } catch (error) {
      setServerError(error.message);
    }
  };

  return (
    <form className={s.form} onSubmit={handleSubmit(onSubmit)} noValidate>
      <Input
        type="email"
        autoComplete="email"
        placeholder="Электронная почта"
        aria-label="Электронная почта"
        error={errors.email?.message}
        {...register("email")}
      />
      <PasswordInput
        autoComplete="current-password"
        placeholder="Пароль"
        aria-label="Пароль"
        error={errors.password?.message}
        {...register("password")}
      />

      <p className={s.forgot}>
        Забыли пароль? <Link to={ROUTES.forgotPassword}>Восстановить</Link>
      </p>

      {serverError && (
        <p role="alert" className={s.serverError}>
          {serverError}
        </p>
      )}

      <Button type="submit" variant="light" disabled={isSubmitting} className={s.submit}>
        {isSubmitting ? "Входим…" : "Войти"}
      </Button>
    </form>
  );
}
