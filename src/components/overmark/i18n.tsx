import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { useRouterState } from "@tanstack/react-router";
import { LANGUAGES, MESSAGES, matchLanguage, type LangDir } from "@/lib/overmark/locales";
import { readLang, writeLang } from "@/lib/overmark/storage";

export type MessageKey = keyof typeof MESSAGES.en;

type I18nValue = {
  lang: string | null;
  dir: LangDir;
  picking: boolean;
  setPicking: (open: boolean) => void;
  setLang: (code: string) => void;
  t: (key: MessageKey) => string;
};

const I18nContext = createContext<I18nValue | null>(null);

export function I18nProvider({ children }: { children: ReactNode }) {
  const [lang, setLangState] = useState<string | null>(null);
  const [ready, setReady] = useState(false);
  const [picking, setPicking] = useState(false);

  useEffect(() => {
    const saved = readLang();
    if (saved && MESSAGES[saved]) {
      setLangState(saved);
    } else if (typeof navigator !== "undefined") {
      const tags = navigator.languages?.length ? navigator.languages : [navigator.language];
      setLangState(matchLanguage(tags));
    }
    setReady(true);
  }, []);

  const dir: LangDir = LANGUAGES.find((item) => item.code === lang)?.dir ?? "ltr";

  useEffect(() => {
    if (!ready) return;
    document.documentElement.lang = lang ?? "en";
    document.documentElement.dir = dir;
  }, [ready, lang, dir]);

  const value = useMemo<I18nValue>(() => {
    const code = lang && MESSAGES[lang] ? lang : "en";
    return {
      lang,
      dir,
      picking,
      setPicking,
      setLang: (next) => {
        if (!MESSAGES[next]) return;
        writeLang(next);
        setLangState(next);
        setPicking(false);
      },
      t: (key) => MESSAGES[code]?.[key] ?? MESSAGES.en[key] ?? key,
    };
  }, [lang, dir, picking]);

  const pathname = useRouterState({ select: (state) => state.location.pathname });
  const pitching = pathname === "/coffee";

  return (
    <I18nContext.Provider value={value}>
      {children}
      {ready && !pitching && picking ? <LanguagePicker /> : null}
    </I18nContext.Provider>
  );
}

export function useI18n(): I18nValue {
  const value = useContext(I18nContext);
  if (!value) throw new Error("i18n missing");
  return value;
}

export function LanguagePicker() {
  const { setLang, picking, setPicking, t } = useI18n();
  const [query, setQuery] = useState("");
  const needle = query.trim().toLowerCase();
  const items = LANGUAGES.filter((item) => {
    if (!needle) return true;
    return (
      item.native.toLowerCase().includes(needle) ||
      item.code.toLowerCase().includes(needle)
    );
  });

  return (
    <div className="fixed inset-0 z-40 overflow-y-auto bg-bg">
      <div className="deck mx-auto flex min-h-dvh w-full max-w-md flex-col gap-4">
        <header className="flex flex-col gap-2">
          <p className="text-xs font-semibold tracking-widest text-signal">OVERMARK</p>
          <h1 className="font-display text-5xl leading-none text-fg">{t("chooseLanguage")}</h1>
          <p className="text-sm leading-relaxed text-muted">{t("chooseHint")}</p>
        </header>
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder={t("searchLang")}
          aria-label={t("searchLang")}
          className="h-12 rounded-full border border-line bg-surface px-4 text-base text-fg outline-none"
        />
        {picking ? (
          <button
            type="button"
            onClick={() => setPicking(false)}
            className="h-11 self-start rounded-full border border-line px-4 text-sm text-fg"
          >
            {t("back")}
          </button>
        ) : null}
        <ul className="flex flex-col gap-2 pb-8">
          {items.length === 0 ? <li className="text-sm text-muted">{t("noMatch")}</li> : null}
          {items.map((item) => (
            <li key={item.code}>
              <button
                type="button"
                onClick={() => setLang(item.code)}
                className="flex h-14 w-full items-center justify-between rounded-deck border border-line bg-surface px-4 text-start"
                dir={item.dir}
              >
                <span className="text-base font-semibold text-fg">{item.native}</span>
                <span className="text-xs tracking-widest text-muted">{item.code}</span>
              </button>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
