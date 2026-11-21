import { z } from "zod";
import { EMAIL_PATTERN } from "@/shared/lib/validation";

export const PASSWORD_MIN_LENGTH = 8;

export const registerSchema = z
  .object({
    firstName: z.string().trim().min(1, "Введите имя"),
    lastName: z.string().trim().min(1, "Введите фамилию"),
    email: z
      .string()
      .trim()
      .min(1, "Введите электронную почту")
      .regex(EMAIL_PATTERN, "Проверьте формат почты, например name@mail.ru"),
    password: z
      .string()
      .min(PASSWORD_MIN_LENGTH, `Пароль должен быть не короче ${PASSWORD_MIN_LENGTH} символов`),
    confirmPassword: z.string().min(1, "Повторите пароль"),
    agreement: z.boolean().refine((value) => value === true, "Без согласия зарегистрироваться не получится"),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Пароли не совпадают",
    path: ["confirmPassword"],
  });
