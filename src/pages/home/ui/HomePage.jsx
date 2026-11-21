import { useState } from "react";
import { flushSync } from "react-dom";
import { Hero } from "@/widgets/hero";
import { LANDING_MODE } from "@/features/landing-mode";
import { scrollToId } from "@/shared/lib/scroll";
import { ANCHORS } from "../model/anchors";
import { StartContent } from "./StartContent";
import { MoreContent } from "./MoreContent";

export function HomePage() {
  const [mode, setMode] = useState(LANDING_MODE.START);

  // Переключает режим и прокручивает к нужной секции.
  // flushSync нужен, чтобы новая секция успела появиться в DOM до прокрутки.
  const goTo = (nextMode, anchorId = ANCHORS.content) => {
    flushSync(() => setMode(nextMode));
    scrollToId(anchorId);
  };

  return (
    <>
      <Hero mode={mode} onModeChange={(next) => goTo(next)} contentId={ANCHORS.content} />

      <div id={ANCHORS.content} role="tabpanel">
        {mode === LANDING_MODE.START ? (
          <StartContent
            onShowDirections={() => goTo(LANDING_MODE.MORE, ANCHORS.specialties)}
            onAskQuestion={() => scrollToId(ANCHORS.consultation)}
          />
        ) : (
          <MoreContent onStartTest={() => goTo(LANDING_MODE.START, ANCHORS.careerTest)} />
        )}
      </div>
    </>
  );
}
