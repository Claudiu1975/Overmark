import { DEFAULT_LIMIT_KMH, snapLimit, type Unit } from "./pace";

export const LANG_KEY = "overmark-lang";
export const LIMIT_KEY = "overmark-limit-kmh";
export const UNIT_KEY = "overmark-unit";
const LIMIT_REV_KEY = "overmark-limit-rev";
const LIMIT_REV = "2";

const KEEP = new Set([LANG_KEY, LIMIT_KEY, UNIT_KEY, LIMIT_REV_KEY]);

export function readLang(): string | null {
  try {
    const value = localStorage.getItem(LANG_KEY);
    return value && value.trim() ? value : null;
  } catch {
    return null;
  }
}

export function writeLang(code: string): void {
  localStorage.setItem(LANG_KEY, code);
}

export function readSpeed(): { limitKmh: number; unit: Unit } {
  let limit = DEFAULT_LIMIT_KMH;
  let unit: Unit = "kmh";
  try {
    if (localStorage.getItem(LIMIT_REV_KEY) !== LIMIT_REV) {
      localStorage.setItem(LIMIT_KEY, String(DEFAULT_LIMIT_KMH));
      localStorage.setItem(LIMIT_REV_KEY, LIMIT_REV);
    }
    const raw = localStorage.getItem(LIMIT_KEY);
    const savedUnit = localStorage.getItem(UNIT_KEY);
    if (raw) limit = snapLimit(Number(raw));
    if (savedUnit === "mph") unit = "mph";
  } catch {
    /* storage unavailable */
  }
  return { limitKmh: limit, unit };
}

export function writeSpeed(limitKmh: number, unit: Unit): void {
  try {
    localStorage.setItem(LIMIT_KEY, String(snapLimit(limitKmh)));
    localStorage.setItem(UNIT_KEY, unit);
    localStorage.setItem(LIMIT_REV_KEY, LIMIT_REV);
  } catch {
    /* ignore */
  }
}

/** Drop every Overmark key except language and speed settings. */
export function clearVolatileStorage(): void {
  try {
    sessionStorage.clear();
  } catch {
    /* ignore */
  }
  try {
    const keys = Object.keys(localStorage);
    for (const key of keys) {
      if (key.startsWith("overmark-") && !KEEP.has(key)) localStorage.removeItem(key);
    }
  } catch {
    /* ignore */
  }
}
