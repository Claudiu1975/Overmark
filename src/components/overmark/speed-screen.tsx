import { Link } from "@tanstack/react-router";
import { Gauge, Info, Settings as SettingsIcon } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { useI18n } from "@/components/overmark/i18n";
import { beep, primeAudio, startAlarm, stopAlarm } from "@/lib/overmark/audio";
import { watchGps, type GpsStatus } from "@/lib/overmark/geo";
import {
  ALARM_AFTER_MS,
  DEFAULT_LIMIT_KMH,
  LIMIT_STEP_KMH,
  MARK_COUNT,
  acceptSpeed,
  beepSecond,
  filledMarks,
  formatLimit,
  fromDisplay,
  phaseOf,
  snapLimit,
  toDisplay,
  unitLabel,
  type Unit,
} from "@/lib/overmark/pace";
import { clearVolatileStorage, readFloatSize, readSpeed, writeFloatSize, writeSpeed, type FloatSize } from "@/lib/overmark/storage";

export function SpeedScreen() {
  const { t, setPicking } = useI18n();
  const [limitKmh, setLimitKmh] = useState(DEFAULT_LIMIT_KMH);
  const [unit, setUnit] = useState<Unit>("kmh");
  const [limitText, setLimitText] = useState(formatLimit(DEFAULT_LIMIT_KMH, "kmh"));
  const [mode, setMode] = useState<"gps" | "test">("gps");
  const [running, setRunning] = useState(false);
  const [testKmh, setTestKmh] = useState(0);
  const [gpsKmh, setGpsKmh] = useState(0);
  const [gpsStatus, setGpsStatus] = useState<GpsStatus>("idle");
  const [elapsedMs, setElapsedMs] = useState(0);
  const [beepFlash, setBeepFlash] = useState(0);
  const [hydrated, setHydrated] = useState(false);
  const [minimized, setMinimized] = useState(false);
  const [closed, setClosed] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [floating, setFloating] = useState(false);
  const [floatSize, setFloatSize] = useState<FloatSize>("small");

  const overSince = useRef(0);
  const lastBeep = useRef(0);

  useEffect(() => {
    const saved = readSpeed();
    setUnit(saved.unit);
    setLimitKmh(saved.limitKmh);
    setLimitText(formatLimit(saved.limitKmh, saved.unit));
    setFloatSize(readFloatSize());
    setHydrated(true);
  }, []);

  useEffect(() => {
    if (!hydrated) return;
    writeSpeed(limitKmh, unit);
  }, [hydrated, limitKmh, unit]);

  useEffect(() => {
    if (!(running && mode === "gps") || closed) return;
    return watchGps({
      onSpeed: setGpsKmh,
      onStatus: setGpsStatus,
    });
  }, [running, mode, closed]);

  const speedKmh = mode === "test" ? testKmh : gpsKmh;
  const hasFix = mode === "test" ? running : gpsStatus === "fix";
  const over = running && !closed && hasFix && speedKmh > limitKmh;

  useEffect(() => {
    if (!over) {
      overSince.current = 0;
      lastBeep.current = 0;
      stopAlarm();
      setElapsedMs(0);
      return;
    }
    if (overSince.current === 0) {
      overSince.current = performance.now();
      lastBeep.current = 0;
    }
    const id = window.setInterval(() => {
      const ms = performance.now() - overSince.current;
      setElapsedMs(ms);
      if (ms >= ALARM_AFTER_MS) {
        startAlarm();
        return;
      }
      const sec = beepSecond(ms, lastBeep.current);
      if (sec != null) {
        lastBeep.current = sec;
        beep();
        setBeepFlash(sec);
      }
    }, 80);
    return () => window.clearInterval(id);
  }, [over]);

  useEffect(() => {
    if (mode !== "gps" || !over || closed) return;
    if (typeof Notification === "undefined") return;
    let note: Notification | null = null;
    const sync = () => {
      if (document.visibilityState !== "hidden") {
        note?.close();
        note = null;
        return;
      }
      if (Notification.permission !== "granted" || note) return;
      try {
        note = new Notification("Overmark", {
          body: t("alertOver"),
          tag: "overmark-over",
        });
      } catch {
        note = null;
      }
    };
    sync();
    document.addEventListener("visibilitychange", sync);
    return () => {
      document.removeEventListener("visibilitychange", sync);
      note?.close();
    };
  }, [over, mode, closed, t]);

  useEffect(() => () => stopAlarm(), []);

  const rampId = useRef(0);
  function stopRamp() {
    window.cancelAnimationFrame(rampId.current);
    rampId.current = 0;
  }
  function startRamp() {
    stopRamp();
    const target = limitKmh;
    const t0 = performance.now();
    setTestKmh(0);
    const step = (now: number) => {
      const top = Math.min(250, target + Math.max(10, target * 0.1));
      const v = Math.min(top, (target / 4000) * (now - t0));
      setTestKmh(v);
      rampId.current = v < top ? window.requestAnimationFrame(step) : 0;
    };
    rampId.current = window.requestAnimationFrame(step);
  }
  useEffect(() => () => stopRamp(), []);

  const phase = phaseOf({ running: running && !closed, hasFix, speedKmh, limitKmh, elapsedMs });
  const shown = Math.round(toDisplay(acceptSpeed(speedKmh) ? speedKmh : 0, unit));
  const elapsedLabel = `${(elapsedMs / 1000).toFixed(1)} s`;
  const filled = filledMarks(elapsedMs, over);
  const speedClass =
    phase === "alarm"
      ? "speed-alarm font-display text-8xl leading-none tabular"
      : phase === "idle" || phase === "acquiring"
        ? "font-display text-8xl leading-none text-muted tabular"
        : "font-display text-8xl leading-none text-signal tabular";

  function commitLimit(text: string) {
    const n = Number(text);
    if (!Number.isFinite(n)) {
      setLimitText(formatLimit(limitKmh, unit));
      return;
    }
    const kmh = snapLimit(fromDisplay(n, unit));
    setLimitKmh(kmh);
    setLimitText(formatLimit(kmh, unit));
  }

  function stepLimit(deltaKmh: number) {
    const kmh = snapLimit(limitKmh + deltaKmh);
    setLimitKmh(kmh);
    setLimitText(formatLimit(kmh, unit));
  }

  function selectUnit(next: Unit) {
    setUnit(next);
    setLimitText(formatLimit(limitKmh, next));
  }

  function toggleRun() {
    primeAudio();
    if (running) {
      stopRamp();
      setRunning(false);
      if (mode === "gps") setGpsStatus("idle");
      return;
    }
    if (mode === "gps" && typeof Notification !== "undefined" && Notification.permission === "default") {
      void Notification.requestPermission().catch(() => undefined);
    }
    setElapsedMs(0);
    if (mode === "gps") setGpsStatus("search");
    setRunning(true);
    if (mode === "test") startRamp();
  }

  function onExit() {
    setRunning(false);
    setGpsStatus("idle");
    setMinimized(false);
    setFloating(false);
    setClosed(true);
    stopAlarm();
    clearVolatileStorage();
  }

  function openFromGauge() {
    setFloating(false);
    setMinimized(false);
  }

  const gauge = floating ? (
    <FloatBubble
      size={floatSize}
      speed={shown}
      unit={unitLabel(unit)}
      over={over}
      marks={filled}
      alarm={phase === "alarm"}
      label={t("floatHint")}
      onOpen={openFromGauge}
    />
  ) : null;

  if (closed) {
    return (
      <main className="deck mx-auto flex min-h-dvh w-full max-w-md flex-col justify-center gap-6">
        <p className="text-xs font-semibold tracking-widest text-signal">OVERMARK</p>
        <h1 className="font-display text-6xl leading-none text-fg">{t("closedTitle")}</h1>
        <p className="text-base leading-relaxed text-muted">{t("closedBody")}</p>
        <button
          type="button"
          onClick={() => setClosed(false)}
          className="h-14 rounded-full bg-signal text-base font-semibold text-bg"
        >
          {t("reopen")}
        </button>
      </main>
    );
  }

  if (minimized) {
    return (
      <>
        <main className="deck mx-auto flex min-h-dvh w-full max-w-md flex-col justify-end gap-4 pb-6">
          <section className="flex items-center justify-between gap-4 rounded-deck border border-line bg-surface p-4">
            <div>
              <p className="text-xs font-semibold tracking-widest text-muted">{t("minimized")}</p>
              <p className={`font-display text-5xl leading-none tabular ${phase === "alarm" ? "speed-alarm" : "text-signal"}`}>
                {shown}
              </p>
              <p className="text-sm text-muted">
                {unitLabel(unit)} · {elapsedLabel}
              </p>
            </div>
            <button
              type="button"
              onClick={() => setMinimized(false)}
              className="h-12 rounded-full bg-signal px-5 text-sm font-semibold text-bg"
            >
              {t("restore")}
            </button>
          </section>
        </main>
        {gauge}
      </>
    );
  }

  const status =
    phase === "idle"
      ? t("standby")
      : phase === "acquiring"
        ? t("searching")
        : phase === "under"
          ? t("under")
          : phase === "counting"
            ? t("marking")
            : t("alarm");

  const note =
    mode === "test"
      ? running
        ? t("hintTestOn")
        : t("hintTestIdle")
      : !running
        ? t("hintGpsIdle")
        : gpsStatus === "denied"
          ? t("hintDenied")
          : gpsStatus === "insecure"
            ? t("hintInsecure")
            : gpsStatus === "unavailable"
              ? t("hintNoFix")
              : gpsStatus === "search"
                ? t("hintSearch")
                : phase === "alarm"
                  ? t("hintAlarm")
                  : t("hintLive");

  return (
    <>
    <main className="deck mx-auto flex min-h-dvh w-full max-w-md flex-col gap-5">
      <header className="flex flex-wrap items-center gap-3">
        <div className="flex items-center gap-2">
          <Gauge className="size-5 text-signal" aria-hidden="true" />
          <p className="text-sm font-semibold tracking-widest text-fg">OVERMARK</p>
        </div>
      </header>

      <section className="flex flex-1 flex-col items-center justify-center gap-3 text-center">
        <p
          className={
            "text-xs font-semibold tracking-widest " +
            (phase === "alarm" ? "text-alarm" : phase === "counting" ? "text-signal" : "text-muted")
          }
          aria-live="polite"
        >
          {status}
        </p>
        <p className={speedClass} aria-label={`${shown} ${unitLabel(unit)}`}>
          {shown}
        </p>
        <p className="text-sm tracking-widest text-muted">{unitLabel(unit)}</p>
        <p className="font-display text-5xl leading-none text-fg tabular">{elapsedLabel}</p>
        <div className="mt-2 flex w-full gap-px" aria-hidden="true">
          {Array.from({ length: MARK_COUNT }, (_, i) => {
            const on = i < filled;
            const hot = on && beepFlash === i + 1 && phase === "counting";
            const tone = !on ? "bg-line" : phase === "alarm" ? "bg-alarm" : hot ? "bg-fg" : "bg-signal";
            return <span key={i} className={`h-2 flex-1 rounded-full ${tone}`} />;
          })}
        </div>
      </section>

      <section className="flex flex-col gap-3 rounded-deck border border-line bg-surface p-4">
        <div className="flex items-center justify-between gap-3">
          <p className="text-xs font-semibold tracking-widest text-muted">{t("limit")}</p>
          <div className="flex rounded-full border border-line p-1">
            <UnitButton active={unit === "kmh"} onClick={() => selectUnit("kmh")}>
              km/h
            </UnitButton>
            <UnitButton active={unit === "mph"} onClick={() => selectUnit("mph")}>
              mph
            </UnitButton>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Step label={t("stepDown")} onClick={() => stepLimit(-LIMIT_STEP_KMH)}>
            −0.5
          </Step>
          <input
            className="w-full bg-transparent text-center font-display text-5xl leading-none text-fg tabular outline-none"
            inputMode="decimal"
            aria-label={t("limit")}
            value={limitText}
            onChange={(e) => setLimitText(e.target.value.replace(/[^\d.]/g, ""))}
            onBlur={() => commitLimit(limitText)}
          />
          <Step label={t("stepUp")} onClick={() => stepLimit(LIMIT_STEP_KMH)}>
            +0.5
          </Step>
        </div>
      </section>

      <div className="flex rounded-full border border-line bg-surface p-1">
        <UnitButton
          active={mode === "gps"}
          onClick={() => {
            setMode("gps");
            setRunning(false);
            setGpsStatus("idle");
          }}
        >
          GPS
        </UnitButton>
        <UnitButton
          active={mode === "test"}
          onClick={() => {
            setMode("test");
            setRunning(false);
          }}
        >
          {t("test")}
        </UnitButton>
      </div>

      {mode === "test" ? (
        <label className="flex flex-col gap-2 text-sm text-muted">
          {t("testSpeed")}, {Math.round(toDisplay(testKmh, unit))} {unitLabel(unit)}
          <input
            type="range"
            min={0}
            max={250}
            value={Math.round(toDisplay(testKmh, unit))}
            onChange={(e) => {
              stopRamp();
              const kmh = fromDisplay(Number(e.target.value), unit);
              setTestKmh(acceptSpeed(kmh) ? kmh : 0);
            }}
          />
        </label>
      ) : null}

      <button
        type="button"
        onClick={toggleRun}
        className={
          "h-14 rounded-full text-base font-semibold " +
          (phase === "alarm" ? "bg-alarm text-bg" : "bg-signal text-bg")
        }
      >
        {running ? t("disarm") : mode === "gps" ? t("armGps") : t("armTest")}
      </button>
      <div className="flex gap-2">
        <button
          type="button"
          onClick={() => setMinimized(true)}
          className="h-12 flex-1 rounded-full border border-line bg-surface text-sm font-semibold text-fg"
        >
          {t("minimize")}
        </button>
        <button
          type="button"
          onClick={() => {
            setFloating(true);
            setMinimized(true);
          }}
          className="h-12 flex-1 rounded-full border border-line bg-surface text-sm font-semibold text-fg"
        >
          {t("floatGauge")}
        </button>
        <button
          type="button"
          onClick={onExit}
          className="h-12 flex-1 rounded-full border border-line bg-surface text-sm font-semibold text-fg"
        >
          {t("exit")}
        </button>
      </div>
      <p className="text-center text-sm leading-relaxed text-muted">{note}</p>

      <nav className="flex flex-col gap-2" aria-label={t("settings")}>
        <button
          type="button"
          aria-expanded={settingsOpen}
          onClick={() => setSettingsOpen((open) => !open)}
          className="inline-flex h-12 items-center justify-center gap-2 rounded-full border border-line bg-surface text-sm font-semibold text-fg"
        >
          <SettingsIcon className="size-4 text-signal" aria-hidden="true" />
          {t("settings")}
        </button>
        {settingsOpen ? (
          <div className="flex flex-col gap-2">
            <p className="px-2 pt-1 text-xs font-semibold tracking-widest text-muted">{t("floatSize")}</p>
            <div className="flex gap-2">
              <button
                type="button"
                aria-pressed={floatSize === "small"}
                onClick={() => {
                  setFloatSize("small");
                  writeFloatSize("small");
                }}
                className={
                  "h-12 flex-1 rounded-full border border-line text-sm font-semibold " +
                  (floatSize === "small" ? "bg-signal text-bg" : "bg-surface text-fg")
                }
              >
                {t("floatSmall")}
              </button>
              <button
                type="button"
                aria-pressed={floatSize === "large"}
                onClick={() => {
                  setFloatSize("large");
                  writeFloatSize("large");
                }}
                className={
                  "h-12 flex-1 rounded-full border border-line text-sm font-semibold " +
                  (floatSize === "large" ? "bg-signal text-bg" : "bg-surface text-fg")
                }
              >
                {t("floatLarge")}
              </button>
            </div>
            <p className="px-2 text-sm leading-relaxed text-muted">{t("floatHint")}</p>
            <button
              type="button"
              onClick={() => setPicking(true)}
              className="inline-flex h-12 items-center justify-center rounded-full border border-line bg-surface text-sm text-fg"
            >
              {t("language")}
            </button>
            <Link
              to="/about"
              className="inline-flex h-12 items-center justify-center gap-2 rounded-full border border-line bg-surface text-sm text-fg"
            >
              <Info className="size-4 text-signal" aria-hidden="true" />
              {t("about")}
            </Link>
            <Link
              to="/disclaimer"
              className="inline-flex h-12 items-center justify-center rounded-full border border-line bg-surface text-sm text-fg"
            >
              {t("disclaimerTitle")}
            </Link>
          </div>
        ) : null}
      </nav>
    </main>
    {gauge}
    </>
  );
}

function UnitButton({
  active,
  onClick,
  children,
}: {
  active: boolean;
  onClick: () => void;
  children: string;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={
        "h-10 flex-1 rounded-full text-sm font-semibold " +
        (active ? "bg-signal text-bg" : "text-muted")
      }
      aria-pressed={active}
    >
      {children}
    </button>
  );
}

function Step({
  label,
  onClick,
  children,
}: {
  label: string;
  onClick: () => void;
  children: string;
}) {
  return (
    <button
      type="button"
      aria-label={label}
      onClick={onClick}
      className="h-12 w-16 shrink-0 rounded-full border border-line text-sm font-semibold text-fg"
    >
      {children}
    </button>
  );
}

function FloatBubble({
  size,
  speed,
  unit,
  over,
  marks,
  alarm,
  label,
  onOpen,
}: {
  size: FloatSize;
  speed: number;
  unit: string;
  over: boolean;
  marks: number;
  alarm: boolean;
  label: string;
  onOpen: () => void;
}) {
  const [pos, setPos] = useState({ x: 16, y: 72 });
  const drag = useRef<{ px: number; py: number; x: number; y: number; moved: boolean } | null>(null);
  const wide = size === "large";
  const hot = over || alarm;
  const sweep = `${(Math.min(marks, 45) / 45) * 289} 289`;

  return (
    <button
      type="button"
      aria-label={label}
      className={
        "fixed z-40 flex flex-col items-center justify-center rounded-full bg-bg/95 shadow-lg " +
        (wide ? " size-40" : " size-28")
      }
      style={{ right: pos.x, top: pos.y }}
      onPointerDown={(event) => {
        drag.current = { px: event.clientX, py: event.clientY, x: pos.x, y: pos.y, moved: false };
        event.currentTarget.setPointerCapture(event.pointerId);
      }}
      onPointerMove={(event) => {
        const current = drag.current;
        if (!current) return;
        const dx = event.clientX - current.px;
        const dy = event.clientY - current.py;
        if (Math.abs(dx) > 8 || Math.abs(dy) > 8) current.moved = true;
        setPos({ x: current.x - dx, y: current.y + dy });
      }}
      onPointerUp={() => {
        if (drag.current && !drag.current.moved) onOpen();
        drag.current = null;
      }}
    >
      <svg className="pointer-events-none absolute inset-0 h-full w-full" viewBox="0 0 100 100" aria-hidden="true">
        <circle cx="50" cy="50" r="46" fill="none" stroke="#E8A317" strokeWidth={hot ? 7 : 3} />
        {hot && marks > 0 ? (
          <circle
            cx="50"
            cy="50"
            r="46"
            fill="none"
            stroke="#E23B2F"
            strokeWidth={7}
            strokeDasharray={sweep}
            transform="rotate(-90 50 50)"
          />
        ) : null}
      </svg>
      <span
        className={
          (alarm ? "speed-alarm font-black " : hot ? "font-black text-alarm " : "font-bold text-signal ") +
          (wide ? "font-display text-6xl leading-none tabular" : "font-display text-4xl leading-none tabular")
        }
      >
        {speed}
      </span>
      <span className="text-[10px] tracking-widest text-muted">{unit}</span>
    </button>
  );
}
