import { cn } from "@/shared/lib/cn";
import s from "./Title.module.css";

const SIZES = { xl: s.xl, lg: s.lg, md: s.md };

export function Title({ as: Tag = "h2", size = "lg", uppercase = false, accentLine = false, className, children }) {
  return (
    <Tag className={cn(s.title, SIZES[size], uppercase && s.uppercase, accentLine && s.accentLine, className)}>
      {children}
    </Tag>
  );
}
