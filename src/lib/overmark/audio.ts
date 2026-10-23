let ctx: AudioContext | null = null;
let alarm: { stop: () => void } | null = null;

function context(): AudioContext | null {
  if (typeof window === "undefined") return null;
  if (!ctx) {
    const Ctor = window.AudioContext || (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
    if (!Ctor) return null;
    ctx = new Ctor();
  }
  return ctx;
}

/** Call from the arm tap so later beeps are allowed to play. */
export function primeAudio(): void {
  const c = context();
  if (!c) return;
  if (c.state === "suspended") void c.resume();
}

export function beep(): void {
  const c = context();
  if (!c) return;
  if (c.state === "suspended") void c.resume();
  const osc = c.createOscillator();
  const gain = c.createGain();
  osc.type = "sine";
  osc.frequency.value = 1046;
  const t = c.currentTime;
  gain.gain.setValueAtTime(0.0001, t);
  gain.gain.exponentialRampToValueAtTime(0.22, t + 0.008);
  gain.gain.exponentialRampToValueAtTime(0.0001, t + 0.09);
  osc.connect(gain);
  gain.connect(c.destination);
  osc.start(t);
  osc.stop(t + 0.1);
}

export function startAlarm(): void {
  if (alarm) return;
  const c = context();
  if (!c) return;
  if (c.state === "suspended") void c.resume();
  const osc = c.createOscillator();
  const gain = c.createGain();
  osc.type = "square";
  osc.frequency.value = 740;
  gain.gain.value = 0.07;
  osc.connect(gain);
  gain.connect(c.destination);
  osc.start();
  let high = false;
  const id = window.setInterval(() => {
    high = !high;
    osc.frequency.setValueAtTime(high ? 988 : 622, c.currentTime);
  }, 170);
  alarm = {
    stop() {
      window.clearInterval(id);
      try {
        osc.stop();
      } catch {
        /* already stopped */
      }
      osc.disconnect();
      gain.disconnect();
      alarm = null;
    },
  };
}

export function stopAlarm(): void {
  alarm?.stop();
}
