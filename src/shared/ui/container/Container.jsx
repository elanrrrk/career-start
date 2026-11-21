import { cn } from "@/shared/lib/cn";
import s from "./Container.module.css";

export function Container({ className, children }) {
  return <div className={cn(s.container, className)}>{children}</div>;
}
