// Склеивает классы, пропуская false/undefined: cn("a", isActive && "b")
export const cn = (...classes) => classes.filter(Boolean).join(" ");
