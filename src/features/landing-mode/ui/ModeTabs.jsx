import { cn } from "@/shared/lib/cn";
import { LANDING_MODE } from "../model/modes";
import s from "./ModeTabs.module.css";

const TABS = [
  { value: LANDING_MODE.MORE, label: "подробнее" },
  { value: LANDING_MODE.START, label: "на старт" },
];

export function ModeTabs({ value, onChange, controls, className }) {
  return (
    <div role="tablist" aria-label="Что показать" className={cn(s.tabs, className)}>
      {TABS.map((tab) => {
        const isActive = tab.value === value;
        return (
          <button
            key={tab.value}
            type="button"
            role="tab"
            aria-selected={isActive}
            aria-controls={controls}
            className={cn(s.tab, isActive && s.active)}
            onClick={() => onChange(tab.value)}
          >
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}
