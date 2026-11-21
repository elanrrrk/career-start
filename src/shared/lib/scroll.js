export function scrollToId(id) {
  const prefersReduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  document.getElementById(id)?.scrollIntoView({
    behavior: prefersReduced ? "auto" : "smooth",
    block: "start",
  });
}
