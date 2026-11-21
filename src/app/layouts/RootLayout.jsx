import { Outlet, ScrollRestoration, useMatches } from "react-router";
import { Header } from "@/widgets/header";

export function RootLayout() {
  const matches = useMatches();
  const headerTheme = matches.findLast((match) => match.handle?.headerTheme)?.handle.headerTheme ?? "dark";

  return (
    <>
      <Header theme={headerTheme} />
      <main>
        <Outlet />
      </main>
      <ScrollRestoration />
    </>
  );
}
