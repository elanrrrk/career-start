import { cn } from "@/shared/lib/cn";
import s from "./Button.module.css";

const VARIANTS = { filled: s.filled, light: s.light, outline: s.outline, link: s.link };
const SIZES = { md: s.md, lg: s.lg };

export function Button({
  variant = "filled",
  size = "md",
  uppercase = false,
  type = "button",
  className,
  ...rest
}) {
  return (
    <button
      type={type}
      className={cn(s.button, SIZES[size], VARIANTS[variant], uppercase && s.uppercase, className)}
      {...rest}
    />
  );
}
