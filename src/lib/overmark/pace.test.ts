import assert from "node:assert/strict";
import { describe, it } from "node:test";
import {
  ALARM_AFTER_MS,
  acceptSpeed,
  beepSecond,
  filledMarks,
  fromDisplay,
  phaseOf,
  toDisplay,
} from "./pace.ts";

describe("phaseOf", () => {
  const base = { running: true, hasFix: true, speedKmh: 80, limitKmh: 50, elapsedMs: 0 };

  it("stays idle until armed", () => {
    assert.equal(phaseOf({ ...base, running: false }), "idle");
  });

  it("waits for a fix", () => {
    assert.equal(phaseOf({ ...base, hasFix: false }), "acquiring");
  });

  it("is under at or below the limit", () => {
    assert.equal(phaseOf({ ...base, speedKmh: 50 }), "under");
    assert.equal(phaseOf({ ...base, speedKmh: 49.9 }), "under");
  });

  it("counts, then alarms at 45 seconds", () => {
    assert.equal(phaseOf({ ...base, elapsedMs: 44_999 }), "counting");
    assert.equal(phaseOf({ ...base, elapsedMs: ALARM_AFTER_MS }), "alarm");
  });
});

describe("beepSecond", () => {
  it("marks seconds 1 through 44 once each", () => {
    let last = 0;
    const heard: number[] = [];
    for (let ms = 0; ms < ALARM_AFTER_MS; ms += 100) {
      const sec = beepSecond(ms, last);
      if (sec != null) {
        heard.push(sec);
        last = sec;
      }
    }
    assert.deepEqual(heard, Array.from({ length: 44 }, (_, i) => i + 1));
    assert.equal(beepSecond(ALARM_AFTER_MS, 44), null);
  });
});

describe("marks and units", () => {
  it("fills 45 marks and then holds", () => {
    assert.equal(filledMarks(0, true), 0);
    assert.equal(filledMarks(12_900, true), 12);
    assert.equal(filledMarks(90_000, true), 45);
    assert.equal(filledMarks(90_000, false), 0);
  });

  it("converts mph without drifting the stored km/h", () => {
    const kmh = fromDisplay(62, "mph");
    assert.ok(Math.abs(toDisplay(kmh, "mph") - 62) < 1e-9);
    assert.equal(acceptSpeed(451), false);
    assert.equal(acceptSpeed(120), true);
  });
});
