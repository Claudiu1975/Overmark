import { createFileRoute, Link } from "@tanstack/react-router";
import { ChevronLeft } from "lucide-react";

const UPDATED = "3 November 2026";
const REPO = "https://github.com/Claudiu1975/Overmark";
const ISSUES = "https://github.com/Claudiu1975/Overmark/issues";

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
          Overmark is made by Claudiu Ivan. This page describes the Android app and the page you are reading. The app does not send your location or your speed to anyone.
        </p>
      </header>

      <Section title="What the app is for">
        Overmark reads the speed of this device so it can beep while you stay over a limit you choose, and sound an alarm after 45 seconds. That is the whole purpose.
      </Section>

      <Section title="Location">
        If you allow location, the app reads GPS on the device to calculate speed in kilometres per hour. The reading is used on screen, for the timer, and for a notification if the screen is off and the speed is over the limit. The app does not save a route, a trip, or a history of positions. Coordinates are not uploaded.
      </Section>

      <Section title="What stays on the device">
        Only the language and the speed limit are kept, in the app’s private storage on that device. They are not sent anywhere. When you leave the app, temporary files are cleared. Speed used for the live timer is dropped when the app stops.
      </Section>

      <Section title="What is not collected">
        There is no account, no sign-in, and no analytics. There is no advertising and no server run for this app. The app does not sell data, because it does not receive any. It does not ask for your name, email, contacts, photos, or microphone.
      </Section>

      <Section title="Permissions">
        Location is used only to measure speed, including while the screen is off if you have armed tracking. Notifications are used to show that the speed is over the limit on the lock screen. Vibration may mark an alert. The app does not use the internet to upload anything. If you open the source-code link or the coffee link, that opens in your browser, under that site’s own policy.
      </Section>

      <Section title="Children">
        Overmark is a driving aid for people who operate a vehicle. It is not directed at children, and it does not knowingly collect personal information from children.
      </Section>

      <Section title="Changes">
        If this policy changes, the date at the top of this page will change. The app will keep working the same way unless a new version says otherwise.
      </Section>

      <Section title="Contact">
        Claudiu Ivan. Questions about this policy can be sent through the project’s issue tracker.
      </Section>

      <div className="flex flex-col gap-3">
        <a
          href={ISSUES}
          target="_blank"
          rel="noreferrer"
          className="inline-flex h-12 items-center justify-center rounded-full bg-signal text-sm font-semibold text-bg"
        >
          Contact
        </a>
        <a
          href={REPO}
          target="_blank"
          rel="noreferrer"
          className="inline-flex h-12 items-center justify-center rounded-full border border-line text-sm font-semibold text-fg"
        >
          Source code
        </a>
        <Link
          to="/disclaimer"
          className="inline-flex h-12 items-center justify-center rounded-full border border-line text-sm font-semibold text-fg"
        >
          Disclaimer
        </Link>
      </div>
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
