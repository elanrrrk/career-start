import { SignUpPromo } from "@/widgets/sign-up-promo";
import { CareerTest } from "@/widgets/career-test";
import { ConsultationPromo } from "@/widgets/consultation-promo";
import { ANCHORS } from "../model/anchors";

// Режим «на старт»: светло-фиолетовые экраны
export function StartContent({ onShowDirections, onAskQuestion }) {
  return (
    <>
      <SignUpPromo />
      <CareerTest
        id={ANCHORS.careerTest}
        onShowDirections={onShowDirections}
        onAskQuestion={onAskQuestion}
      />
      <ConsultationPromo id={ANCHORS.consultation} />
    </>
  );
}
