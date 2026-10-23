import { acceptSpeed } from "./pace.ts";

export type GpsStatus = "idle" | "search" | "fix" | "denied" | "unavailable" | "insecure";

function meters(
  a: { lat: number; lon: number },
  b: { lat: number; lon: number },
): number {
  const R = 6_371_000;
  const p1 = (a.lat * Math.PI) / 180;
  const p2 = (b.lat * Math.PI) / 180;
  const dphi = ((b.lat - a.lat) * Math.PI) / 180;
  const dl = ((b.lon - a.lon) * Math.PI) / 180;
  const h = Math.sin(dphi / 2) ** 2 + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) ** 2;
  return 2 * R * Math.asin(Math.min(1, Math.sqrt(h)));
}

export function watchGps(handlers: {
  onSpeed: (kmh: number) => void;
  onStatus: (status: GpsStatus) => void;
}): () => void {
  if (typeof navigator === "undefined" || !navigator.geolocation) {
    handlers.onStatus("unavailable");
    return () => {};
  }
  if (typeof window !== "undefined" && !window.isSecureContext) {
    handlers.onStatus("insecure");
    return () => {};
  }

  handlers.onStatus("search");
  let last: { lat: number; lon: number; t: number; acc: number } | null = null;

  const id = navigator.geolocation.watchPosition(
    (pos) => {
      const c = pos.coords;
      let mps: number | null = typeof c.speed === "number" && c.speed >= 0 ? c.speed : null;
      if (mps == null && last) {
        const dt = (pos.timestamp - last.t) / 1000;
        if (dt >= 0.5 && c.accuracy <= 45 && last.acc <= 45) {
          mps = meters(last, { lat: c.latitude, lon: c.longitude }) / dt;
        }
      }
      last = {
        lat: c.latitude,
        lon: c.longitude,
        t: pos.timestamp,
        acc: c.accuracy,
      };
      if (mps == null || !Number.isFinite(mps)) {
        handlers.onStatus("fix");
        return;
      }
      const kmh = Math.max(0, mps * 3.6);
      if (!acceptSpeed(kmh)) return;
      handlers.onStatus("fix");
      handlers.onSpeed(kmh);
    },
    (err) => {
      handlers.onStatus(err.code === 1 ? "denied" : "unavailable");
    },
    { enableHighAccuracy: true, maximumAge: 0, timeout: 20000 },
  );

  return () => navigator.geolocation.clearWatch(id);
}
