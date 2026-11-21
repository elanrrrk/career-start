import { AboutPlatform } from "@/widgets/about-platform";
import { StudentProblems } from "@/widgets/student-problems";
import { HowItWorks } from "@/widgets/how-it-works";
import { Specialties } from "@/widgets/specialties";
import { ANCHORS } from "../model/anchors";

// Режим «подробнее»: тёмные экраны
export function MoreContent({ onStartTest }) {
  return (
    <>
      <AboutPlatform />
      <StudentProblems />
      <HowItWorks />
      <Specialties id={ANCHORS.specialties} onStartTest={onStartTest} />
    </>
  );
}
