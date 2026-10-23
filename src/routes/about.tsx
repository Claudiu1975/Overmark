import { createFileRoute, Link } from "@tanstack/react-router";
import { ChevronLeft } from "lucide-react";
import { useI18n } from "@/components/overmark/i18n";
import { APP_VERSION, AUTHOR } from "@/lib/overmark/pace";

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

      <header className="flex flex-col gap-2">
        <p className="text-xs font-semibold tracking-widest text-signal">OVERMARK</p>
        <h1 className="font-display text-6xl leading-none text-fg">
          {t("version")} {APP_VERSION}
        </h1>
      </header>

      <section className="flex flex-col gap-2 border-t border-line pt-6">
        <h2 className="text-xs font-semibold tracking-widest text-muted">{t("author")}</h2>
        <p className="font-display text-5xl leading-none text-fg">{AUTHOR}</p>
      </section>

      <section className="flex flex-col gap-2">
        <h2 className="text-xs font-semibold tracking-widest text-muted">{t("disclaimerTitle")}</h2>
        <p className="text-base leading-relaxed text-muted">{t("disclaimer")}</p>
      </section>

      <section className="flex flex-col gap-2">
        <h2 className="text-xs font-semibold tracking-widest text-muted">{t("privacyTitle")}</h2>
        <p className="text-base leading-relaxed text-muted">{t("privacy")}</p>
      </section>

      <p className="text-base leading-relaxed text-muted">{t("how")}</p>
      <p className="text-base leading-relaxed text-muted">{t("apkNote")}</p>

      <a
        href="/overmark-1.1.0.apk"
        download="Overmark-1.1.0.apk"
        className="inline-flex h-14 items-center justify-center rounded-full bg-signal text-base font-semibold text-bg"
      >
        {t("download")}
      </a>
    </main>
  );
}
