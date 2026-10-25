import { Link } from "@tanstack/react-router";
import { ChevronLeft, ChevronRight, Coffee, Copy, Gauge } from "lucide-react";
import { useCallback, useEffect, useState } from "react";

export const PAGE_TEXT = `Overmark is a personal speed reminder.

When speed stays over a limit you set, a short beep marks every second. After 45 seconds an alarm sounds and the live speed flashes red. Drop under the limit and the count starts over.

The limit starts at 90 km/h and moves in steps of 0.5 km/h. The first launch asks you to choose a language. You can change it later.

Nothing is collected. There is no account, no analytics, and no server. Location and speed stay on the device only while Overmark is running, and they are dropped when you exit. The only things kept are your language and your speed limit.

Overmark is a driving aid, not a certified speedometer. You alone follow the road, the posted limit, and safe driving.

If it is useful, a coffee keeps the next version moving.

Claudiu Ivan`;

const SLIDES: {
  kicker: string;
  title: string;
  body: string;
  image: string;
  points?: string[];
  ask?: boolean;
}[] = [
  {
    kicker: "Overmark",
    title: "A reminder that stays on the phone.",
    body: "Built by Claudiu Ivan. Free to use. The only ask is a coffee, if it earns one.",
    image: "/deck/road.jpg",
  },
  {
    kicker: "The moment",
    title: "The number is not the point.",
    body: "Once you are over the limit, what matters is how long you have been there.",
    image: "/deck/gauge.jpg",
  },
  {
    kicker: "The rule",
    title: "One beep. Then forty-five.",
    body: "Each second over the limit gets a short beep. At 45 seconds the alarm sounds and the live speed flashes red. Drop under the limit and the count starts over.",
    image: "/deck/road.jpg",
    points: ["Short beep, seconds 1–44", "Alarm and a red flash at 45", "Reset the moment you are under"],
  },
  {
    kicker: "Set once",
    title: "90 km/h. Half a kilometer at a time.",
    body: "That is where the limit starts. It moves in steps of 0.5 km/h, in kilometers or miles. Bench mode lets you hear it before you drive.",
    image: "/deck/gauge.jpg",
  },
  {
    kicker: "Privacy",
    title: "Nothing leaves the phone.",
    body: "No account. No analytics. No server. Exit wipes temporary files. The only things kept are the language and the speed limit.",
    image: "/deck/road.jpg",
  },
  {
    kicker: "Language",
    title: "You pick it. It stays.",
    body: "The first start asks for a language. Fifty-three are on the phone. Change it later, from the same list. Nothing is sent.",
    image: "/deck/gauge.jpg",
  },
  {
    kicker: "Where it runs",
    title: "Browser, or the Android app.",
    body: "Try it in the browser with Bench. On a phone, the signed app needs Android 8 or newer. Beeps use media volume. The alarm uses the alarm stream.",
    image: "/deck/road.jpg",
  },
  {
    kicker: "Buy me a coffee",
    title: "If Overmark is useful, that is the ask.",
    body: "Paste the note below onto the page. It says what the app does, what it refuses to collect, and who made it.",
    image: "/deck/cup.jpg",
    ask: true,
  },
];

export function CoffeeDeck() {
  const [index, setIndex] = useState(0);
  const [copied, setCopied] = useState(false);
  const slide = SLIDES[index]!;

  const go = useCallback((next: number) => {
    setIndex((next + SLIDES.length) % SLIDES.length);
  }, []);

  useEffect(() => {
    function onKey(event: KeyboardEvent) {
      const tag = (event.target as HTMLElement | null)?.tagName;
      if (tag === "TEXTAREA" || tag === "INPUT" || tag === "BUTTON") return;
      if (event.key === "ArrowRight" || event.key === " ") {
        event.preventDefault();
        go(index + 1);
      } else if (event.key === "ArrowLeft") {
        event.preventDefault();
        go(index - 1);
      }
    }
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [go, index]);

  useEffect(() => {
    let startX = 0;
    let tracking = false;
    function down(event: PointerEvent) {
      if ((event.target as HTMLElement).closest("button, a, textarea")) {
        tracking = false;
        return;
      }
      tracking = true;
      startX = event.clientX;
    }
    function up(event: PointerEvent) {
      if (!tracking) return;
      tracking = false;
      const delta = event.clientX - startX;
      if (Math.abs(delta) < 48) return;
      go(index + (delta < 0 ? 1 : -1));
    }
    window.addEventListener("pointerdown", down);
    window.addEventListener("pointerup", up);
    return () => {
      window.removeEventListener("pointerdown", down);
      window.removeEventListener("pointerup", up);
    };
  }, [go, index]);

  async function copyPage() {
    try {
      await navigator.clipboard.writeText(PAGE_TEXT);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 1800);
    } catch {
      setCopied(false);
    }
  }

  return (
    <main className="grid min-h-dvh lg:grid-cols-[minmax(0,1.15fr)_minmax(0,0.85fr)]">
      <section className="order-2 flex min-h-[70dvh] flex-col justify-between gap-8 px-6 py-8 sm:px-10 lg:order-1 lg:px-14 lg:py-12">
        <header className="flex items-center justify-between gap-4">
          <Link to="/" className="inline-flex items-center gap-2 text-sm font-semibold tracking-widest text-fg">
            <Gauge className="size-4 text-signal" aria-hidden="true" />
            OVERMARK
          </Link>
          <p className="text-xs font-semibold tracking-widest text-muted">
            {String(index + 1).padStart(2, "0")} / {String(SLIDES.length).padStart(2, "0")}
          </p>
        </header>

        <article className="flex max-w-xl flex-col gap-5" aria-live="polite">
          <p className="text-xs font-semibold tracking-widest text-signal">{slide.kicker}</p>
          <h1
            className={
              "font-display leading-none text-fg " +
              (slide.ask ? "text-4xl sm:text-5xl" : "text-5xl sm:text-7xl")
            }
          >
            {slide.title}
          </h1>
          <p className="text-base leading-relaxed text-muted sm:text-lg">{slide.body}</p>
          {slide.points ? (
            <ul className="flex flex-col gap-2 text-sm text-fg">
              {slide.points.map((point) => (
                <li key={point} className="flex items-center gap-3">
                  <span className="size-1.5 rounded-full bg-signal" aria-hidden="true" />
                  {point}
                </li>
              ))}
            </ul>
          ) : null}
          {slide.ask ? (
            <div className="flex flex-col gap-3">
              <textarea
                readOnly
                value={PAGE_TEXT}
                aria-label="Text for the Buy Me a Coffee page"
                className="h-28 resize-none rounded-deck border border-line bg-surface p-4 text-sm leading-relaxed text-muted"
              />
              <button
                type="button"
                onClick={copyPage}
                className="inline-flex h-12 items-center justify-center gap-2 self-start rounded-full bg-signal px-5 text-sm font-semibold text-bg"
              >
                {copied ? (
                  "Copied"
                ) : (
                  <>
                    <Copy className="size-4" aria-hidden="true" />
                    Copy page text
                  </>
                )}
              </button>
            </div>
          ) : null}
        </article>

        <footer className="flex flex-col gap-5">
          <div className="flex gap-1" aria-hidden="true">
            {SLIDES.map((item, mark) => (
              <span
                key={item.kicker}
                className={`h-1 flex-1 rounded-full ${mark <= index ? "bg-signal" : "bg-line"}`}
              />
            ))}
          </div>
          <div className="flex items-center justify-between gap-3">
            <button
              type="button"
              onClick={() => go(index - 1)}
              className="inline-flex h-12 items-center gap-1 rounded-full border border-line px-4 text-sm text-fg"
              aria-label="Previous slide"
            >
              <ChevronLeft className="size-4" aria-hidden="true" />
              Back
            </button>
            <p className="hidden text-xs tracking-widest text-muted sm:block">Arrows, or swipe</p>
            <button
              type="button"
              onClick={() => go(index + 1)}
              className="inline-flex h-12 items-center gap-1 rounded-full bg-fg px-4 text-sm font-semibold text-bg"
              aria-label="Next slide"
            >
              Next
              <ChevronRight className="size-4" aria-hidden="true" />
            </button>
          </div>
        </footer>
      </section>

      <figure className={`relative order-1 lg:order-2 lg:h-auto ${slide.ask ? "h-32 sm:h-40" : "h-56 sm:h-72"}`}>
        <img
          src={slide.image}
          alt=""
          className="absolute inset-0 h-full w-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-bg via-transparent to-transparent lg:bg-gradient-to-l" />
        {slide.ask ? (
          <figcaption className="absolute bottom-6 left-6 inline-flex items-center gap-2 text-sm font-semibold tracking-widest text-fg">
            <Coffee className="size-4 text-signal" aria-hidden="true" />
            Claudiu Ivan
          </figcaption>
        ) : null}
      </figure>
    </main>
  );
}
