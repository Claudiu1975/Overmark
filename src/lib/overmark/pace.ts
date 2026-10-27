export const APP_VERSION = "1.3.0";
export const AUTHOR = "Claudiu Florian Ivan";
export const ALARM_AFTER_MS = 45_000;
export const MARK_COUNT = 45;
export const MAX_PLAUSIBLE_KMH = 450;
export const DEFAULT_LIMIT_KMH = 90;
export const LIMIT_STEP_KMH = 0.5;

export type Phase = "idle" | "acquiring" | "under" | "counting" | "alarm";
export type Unit = "kmh" | "mph";

const MPH = 1.609344;

export function toDisplay(kmh: number, unit: Unit): number {
  return unit === "mph" ? kmh / MPH : kmh;
}

export function fromDisplay(value: number, unit: Unit): number {
  return unit === "mph" ? value * MPH : value;
}

export function unitLabel(unit: Unit): string {
  return unit === "mph" ? "mph" : "km/h";
}

export function snapLimit(kmh: number): number {
  if (!Number.isFinite(kmh)) return DEFAULT_LIMIT_KMH;
  const snapped = Math.round(kmh * 2) / 2;
  return Math.min(320, Math.max(0.5, snapped));
}

export function formatLimit(kmh: number, unit: Unit): string {
  const shown = Math.round(toDisplay(kmh, unit) * 10) / 10;
  return shown.toFixed(1);
}

export function phaseOf(opts: {
  running: boolean;
  hasFix: boolean;
  speedKmh: number;
  limitKmh: number;
  elapsedMs: number;
}): Phase {
  if (!opts.running) return "idle";
  if (!opts.hasFix) return "acquiring";
  if (!(opts.speedKmh > opts.limitKmh)) return "under";
  if (opts.elapsedMs >= ALARM_AFTER_MS) return "alarm";
  return "counting";
}

/** Short beep on each whole second from 1 through 44. Alarm owns second 45. */
export function beepSecond(elapsedMs: number, lastBeepSec: number): number | null {
  if (elapsedMs >= ALARM_AFTER_MS) return null;
  const sec = Math.floor(elapsedMs / 1000);
  if (sec >= 1 && sec < MARK_COUNT && sec !== lastBeepSec) return sec;
  return null;
}

export function filledMarks(elapsedMs: number, over: boolean): number {
  if (!over) return 0;
  return Math.min(MARK_COUNT, Math.floor(elapsedMs / 1000));
}

export function acceptSpeed(kmh: number): boolean {
  return Number.isFinite(kmh) && kmh >= 0 && kmh <= MAX_PLAUSIBLE_KMH;
}
