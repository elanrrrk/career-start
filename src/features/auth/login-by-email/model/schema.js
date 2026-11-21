import { z } from "zod";
import { EMAIL_PATTERN } from "@/shared/lib/validation";

export const loginSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, "Введите электронную почту")
    .regex(EMAIL_PATTERN, "Проверьте формат почты, например name@mail.ru"),
  password: z.string().min(1, "Введите пароль"),
});
