import { createFileRoute, Link } from "@tanstack/react-router";
import { ChevronLeft } from "lucide-react";
import { useI18n } from "@/components/overmark/i18n";

export const Route = createFileRoute("/about")({
  component: About,
  head: () => ({ meta: [{ title: "About · Overmark" }] }),
});

function About() {
  const { t } = useI18n();
  return (
    <main className="deck mx-auto flex min-h-dvh w-full max-w-md flex-col gap-8">
      <Link to="/" className="inline-flex h-11 w-fit items-center gap-1 text-sm text-muted">
        <ChevronLeft className="size-4" aria-hidden="true" />
        {t("back")}
      </Link>

      <section className="flex flex-col gap-4">
        <p className="text-xs font-semibold tracking-widest text-signal">OVERMARK</p>
        <h1 className="font-display text-5xl leading-none text-fg">{t("about")}</h1>
        <p className="text-base leading-relaxed text-fg">{t("proBody")}</p>
        <img src="/pro/nav-gauge.jpg" alt={t("proPhoto")} className="w-full rounded-2xl" />
      </section>

      <section id="legal" className="flex flex-col gap-3 border-t border-line pt-6">
        <h2 className="font-display text-4xl leading-none text-fg">{t("disclaimerTitle")}</h2>
        <p className="text-base leading-relaxed text-muted">{t("disclaimer")}</p>
        <h2 className="pt-4 text-xs font-semibold tracking-widest text-muted">{t("privacyTitle")}</h2>
        <p className="text-base leading-relaxed text-muted">{t("privacy")}</p>
      </section>
    </main>
  );
}
