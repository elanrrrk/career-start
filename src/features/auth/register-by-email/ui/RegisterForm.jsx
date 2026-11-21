"use no memo"; // react-hook-form пока плохо дружит с React Compiler, отключаем его для этого файла

import { useState } from "react";
import { Link } from "react-router";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button, Checkbox, Input, PasswordInput } from "@/shared/ui";
import { ROUTES } from "@/shared/config/routes";
import { useSessionStore } from "@/entities/session";
import { registerSchema } from "../model/schema";
import { registerUser } from "../api/register";
import s from "./RegisterForm.module.css";

export function RegisterForm({ onSuccess }) {
  const setSession = useSessionStore((state) => state.setSession);
  const [serverError, setServerError] = useState(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      password: "",
      confirmPassword: "",
      agreement: false,
    },
  });

  const onSubmit = async (values) => {
    setServerError(null);
    try {
      const session = await registerUser(values);
      setSession(session);
      onSuccess?.();
    } catch (error) {
      setServerError(error.message);
    }
  };

  return (
    <form className={s.form} onSubmit={handleSubmit(onSubmit)} noValidate>
      <div className={s.row}>
        <Input
          autoComplete="given-name"
          placeholder="Имя"
          aria-label="Имя"
          error={errors.firstName?.message}
          {...register("firstName")}
        />
        <Input
          autoComplete="family-name"
          placeholder="Фамилия"
          aria-label="Фамилия"
          error={errors.lastName?.message}
          {...register("lastName")}
        />
      </div>
      <Input
        type="email"
        autoComplete="email"
        placeholder="Электронная почта"
        aria-label="Электронная почта"
        error={errors.email?.message}
        {...register("email")}
      />
      <PasswordInput
        autoComplete="new-password"
        placeholder="Пароль"
        aria-label="Пароль"
        error={errors.password?.message}
        {...register("password")}
      />
      <PasswordInput
        autoComplete="new-password"
        placeholder="Подтвердить пароль"
        aria-label="Подтвердить пароль"
        error={errors.confirmPassword?.message}
        {...register("confirmPassword")}
      />

      <Checkbox error={errors.agreement?.message} {...register("agreement")}>
        Я подтверждаю своё согласие с условиями <Link to={ROUTES.offer}>публичной оферты</Link> и согласен
        на обработку персональных данных в соответствии с{" "}
        <Link to={ROUTES.privacy}>политикой конфиденциальности</Link>
      </Checkbox>

      {serverError && (
        <p role="alert" className={s.serverError}>
          {serverError}
        </p>
      )}

      <Button type="submit" variant="light" disabled={isSubmitting} className={s.submit}>
        {isSubmitting ? "Регистрируем…" : "Зарегистрироваться"}
      </Button>
    </form>
  );
}
