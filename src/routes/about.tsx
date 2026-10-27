import { createFileRoute, Link } from "@tanstack/react-router";
import { ChevronLeft } from "lucide-react";
import { useI18n } from "@/components/overmark/i18n";
import { APP_VERSION } from "@/lib/overmark/pace";

const PAYPAL = "https://www.paypal.me/G0anga";
const REPO = "https://github.com/Claudiu1975/Overmark";
const AUTHOR_NAME = "Claudiu Ivan";

export const Route = createFileRoute("/about")({
  component: About,
  head: () => ({ meta: [{ title: "About · Overmark" }] }),
});

function About() {
  const { t } = useI18n();
  const bio = t("authorStory").replaceAll("Claudiu Florian Ivan", AUTHOR_NAME);
  return (
    <main className="deck mx-auto flex min-h-dvh w-full max-w-md flex-col">
      <Link to="/" className="inline-flex h-11 w-fit items-center gap-1 text-sm text-muted">
        <ChevronLeft className="size-4" aria-hidden="true" />
        {t("back")}
      </Link>

      <section className="flex flex-col gap-4 pt-8">
        <p className="text-xs font-semibold tracking-widest text-signal">OVERMARK</p>
        <div className="h-1.5 w-full rounded-full bg-signal" aria-hidden="true" />
        <h1 className="font-display text-5xl leading-none text-fg">
          {t("version")} {APP_VERSION}
        </h1>
        <p className="text-base leading-relaxed text-muted">{t("appStory")}</p>
        <p className="text-base leading-relaxed text-muted">{t("how")}</p>
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

      <section className="mt-auto flex flex-col gap-4 pt-16">
        <h2 className="text-xs font-semibold tracking-widest text-muted">{t("author")}</h2>
        <div className="h-1.5 w-full rounded-full bg-signal" aria-hidden="true" />
        <p className="font-display text-5xl leading-none text-fg">{AUTHOR_NAME}</p>
        <p className="text-base leading-relaxed text-muted">{bio}</p>
        <p className="text-base leading-relaxed text-fg">{t("coffeeInvite")}</p>
        <a
          href={PAYPAL}
          target="_blank"
          rel="noreferrer"
          className="inline-flex h-14 items-center justify-center rounded-full bg-signal text-base font-semibold text-bg"
        >
          {t("coffeeButton")}
        </a>
        <a href={PAYPAL} target="_blank" rel="noreferrer" className="block">
          <img
            src="/paypal-qr.png"
            alt={`${AUTHOR_NAME} · ${t("coffeeButton")}`}
            className="w-full rounded-deck bg-white"
          />
        </a>
      </section>
    </main>
  );
}
