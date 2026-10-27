import { createFileRoute, Link } from "@tanstack/react-router";
import { ChevronLeft } from "lucide-react";
import { useI18n } from "@/components/overmark/i18n";

export const Route = createFileRoute("/disclaimer")({
  component: Disclaimer,
  head: () => ({ meta: [{ title: "Disclaimer · Overmark" }] }),
});

function Disclaimer() {
  const { t } = useI18n();
  return (
    <main className="deck mx-auto flex min-h-dvh w-full max-w-md flex-col gap-8">
      <Link to="/" className="inline-flex h-11 w-fit items-center gap-1 text-sm text-muted">
        <ChevronLeft className="size-4" aria-hidden="true" />
        {t("back")}
      </Link>

      <header className="flex flex-col gap-3">
        <p className="text-xs font-semibold tracking-widest text-signal">OVERMARK</p>
        <h1 className="font-display text-6xl leading-none text-fg">{t("disclaimerTitle")}</h1>
        <p className="text-base leading-relaxed text-muted">{t("disclaimer")}</p>
      </header>

      <section className="flex flex-col gap-2 border-t border-line pt-6">
        <h2 className="text-xs font-semibold tracking-widest text-muted">{t("privacyTitle")}</h2>
        <p className="text-base leading-relaxed text-muted">{t("privacy")}</p>
      </section>

      <Link
        to="/about"
        className="inline-flex h-12 items-center justify-center rounded-full border border-line text-sm font-semibold text-fg"
      >
        {t("about")}
      </Link>
    </main>
  );
}
