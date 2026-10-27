import { createFileRoute, Link } from "@tanstack/react-router";
import { ChevronLeft } from "lucide-react";
import { useI18n } from "@/components/overmark/i18n";
import { APP_VERSION, AUTHOR } from "@/lib/overmark/pace";

const PAYPAL = "https://www.paypal.me/G0anga";
const REPO = "https://github.com/Claudiu1975/Overmark";

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

      <header className="flex flex-col gap-3">
        <p className="text-xs font-semibold tracking-widest text-signal">OVERMARK</p>
        <h1 className="font-display text-6xl leading-none text-fg">
          {t("version")} {APP_VERSION}
        </h1>
        <p className="text-base leading-relaxed text-muted">{t("appStory")}</p>
      </header>

      <section className="flex flex-col gap-3 border-t border-line pt-6">
        <h2 className="text-xs font-semibold tracking-widest text-muted">{t("author")}</h2>
        <p className="font-display text-5xl leading-none text-fg">{AUTHOR}</p>
        <p className="text-base leading-relaxed text-muted">{t("authorStory")}</p>
        <p className="text-base leading-relaxed text-fg">{t("coffeeInvite")}</p>
        <a
          href={PAYPAL}
          target="_blank"
          rel="noreferrer"
          className="inline-flex h-14 items-center justify-center rounded-full bg-signal text-base font-semibold text-bg"
        >
          {t("coffeeButton")}
        </a>
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

      <section className="flex flex-col gap-3">
        <p className="text-base leading-relaxed text-fg">{t("openSource")}</p>
        <a
          href={REPO}
          target="_blank"
          rel="noreferrer"
          className="inline-flex h-12 items-center justify-center rounded-full border border-line text-sm font-semibold text-fg"
        >
          {t("sourceCode")}
        </a>
      </section>

      <a href={PAYPAL} target="_blank" rel="noreferrer" className="block">
        <img
          src="/paypal-qr.png"
          alt={`${AUTHOR} · ${t("coffeeButton")}`}
          className="w-full rounded-deck bg-white"
        />
      </a>

      <p className="text-base leading-relaxed text-muted">{t("apkNote")}</p>

      <a
        href="/overmark-1.2.0.apk"
        download="Overmark-1.2.0.apk"
        className="inline-flex h-14 items-center justify-center rounded-full border border-line text-base font-semibold text-fg"
      >
        {t("download")}
      </a>
    </main>
  );
}
