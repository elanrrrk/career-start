import { useState } from "react";
import { Section } from "@/shared/ui";
import { TestIntro } from "./TestIntro";
import { TestResult } from "./TestResult";

const STEP = { INTRO: "intro", RESULT: "result" };

export function CareerTest({ id, onShowDirections, onAskQuestion }) {
  const [step, setStep] = useState(STEP.INTRO);

  return (
    <Section id={id} tone="glow" fullHeight>
      {step === STEP.INTRO ? (
        // TODO: когда появится макет вопросов, «Начать» будет открывать сам тест
        <TestIntro onStart={() => setStep(STEP.RESULT)} onSkip={onShowDirections} />
      ) : (
        <TestResult onShowDirections={onShowDirections} onAskQuestion={onAskQuestion} />
      )}
    </Section>
  );
}
