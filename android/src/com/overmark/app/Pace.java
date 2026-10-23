package com.overmark.app;

/** Same timing rules as the browser build. */
public final class Pace {
    public static final long ALARM_AFTER_MS = 45_000L;
    public static final int MARK_COUNT = 45;
    public static final float MAX_PLAUSIBLE_KMH = 450f;
    private static final float MPH = 1.609344f;

    private Pace() {}

    public static boolean overLimit(float speedKmh, float limitKmh) {
        return speedKmh > limitKmh;
    }

    public static boolean acceptSpeed(float kmh) {
        return kmh >= 0f && kmh <= MAX_PLAUSIBLE_KMH;
    }

    /** Whole seconds 1..44 that should chirp. -1 if none. */
    public static int beepSecond(long elapsedMs, int lastBeepSec) {
        if (elapsedMs >= ALARM_AFTER_MS) return -1;
        int sec = (int) (elapsedMs / 1000L);
        if (sec >= 1 && sec < MARK_COUNT && sec != lastBeepSec) return sec;
        return -1;
    }

    public static int filledMarks(long elapsedMs, boolean over) {
        if (!over || elapsedMs < 0L) return 0;
        int filled = (int) (elapsedMs / 1000L);
        return Math.min(MARK_COUNT, filled);
    }

    public static float toDisplay(float kmh, boolean mph) {
        return mph ? kmh / MPH : kmh;
    }

    public static float fromDisplay(float value, boolean mph) {
        return mph ? value * MPH : value;
    }
}
