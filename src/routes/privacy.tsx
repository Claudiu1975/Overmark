import { createFileRoute, Link } from "@tanstack/react-router";
import { ChevronLeft } from "lucide-react";

const UPDATED = "4 November 2026";

export const Route = createFileRoute("/privacy")({
  component: Privacy,
  head: () => ({ meta: [{ title: "Privacy · Overmark" }] }),
});

function Privacy() {
  return (
    <main className="deck mx-auto flex min-h-dvh w-full max-w-md flex-col gap-8">
      <Link to="/" className="inline-flex h-11 w-fit items-center gap-1 text-sm text-muted">
        <ChevronLeft className="size-4" aria-hidden="true" />
        Back
      </Link>

      <header className="flex flex-col gap-3">
        <p className="text-xs font-semibold tracking-widest text-signal">OVERMARK</p>
        <h1 className="font-display text-6xl leading-none text-fg">Privacy</h1>
        <p className="text-sm text-muted">Updated {UPDATED}</p>
        <p className="text-base leading-relaxed text-muted">
          Overmark is published by goanga.ro. This page describes the app. It does not send your location or your speed to anyone.
        </p>
      </header>

      <Section title="What the app is for">
        Overmark reads the speed of this device so it can beep while you stay over a limit you choose, and sound an alarm after 45 seconds. That is the whole purpose.
      </Section>
      <Section title="Location">
        If you allow location, the app reads GPS on the device to calculate speed. The reading is used on screen, for the timer, and for a notification if the screen is off and the speed is over the limit. The app does not save a route, a trip, or a history of positions. Coordinates are not uploaded.
      </Section>
      <Section title="What stays on the device">
        Only the language and the speed limit are kept, on that device. They are not sent anywhere. When you leave the app, temporary files are cleared. Speed used for the live timer is dropped when the app stops.
      </Section>
      <Section title="What is not collected">
        There is no account, no sign-in, and no analytics. There is no advertising and no server run for this app. It does not ask for your name, email, contacts, photos, or microphone.
      </Section>
      <Section title="Contact">
        goanga.ro. Questions about this policy can be sent to admin@goanga.ro.
      </Section>

      <a
        href="mailto:admin@goanga.ro"
        className="inline-flex h-12 items-center justify-center rounded-full bg-signal text-sm font-semibold text-bg"
      >
        admin@goanga.ro
      </a>
    </main>
  );
}

function Section({ title, children }: { title: string; children: string }) {
  return (
    <section className="flex flex-col gap-2 border-t border-line pt-6">
      <h2 className="text-xs font-semibold tracking-widest text-muted">{title}</h2>
      <p className="text-base leading-relaxed text-fg">{children}</p>
    </section>
  );
}
