import { createFileRoute, Link } from "@tanstack/react-router";
import { ChevronLeft } from "lucide-react";

export const Route = createFileRoute("/video")({
  component: VideoPage,
  head: () => ({ meta: [{ title: "Video · Overmark" }] }),
});

function VideoPage() {
  return (
    <main className="deck mx-auto flex min-h-dvh w-full max-w-md flex-col gap-6">
      <Link to="/" className="inline-flex h-11 w-fit items-center gap-1 text-sm text-muted">
        <ChevronLeft className="size-4" aria-hidden="true" />
        Back
      </Link>
      <p className="text-xs font-semibold tracking-widest text-signal">OVERMARK</p>
      <h1 className="font-display text-5xl leading-none text-fg">The video</h1>
      <p className="text-base leading-relaxed text-muted">
        Tap the yellow button. Your phone saves it as overmark-canyon-30s.mp4, usually in Downloads.
      </p>
      <a
        href="/overmark-canyon-30s.mp4"
        download="overmark-canyon-30s.mp4"
        className="flex h-16 items-center justify-center rounded-full bg-signal text-lg font-semibold text-bg"
      >
        Download the video
      </a>
      <video
        className="w-full overflow-hidden rounded-deck border border-line bg-black"
        controls
        playsInline
        preload="metadata"
        src="/overmark-canyon-30s.mp4"
      />
    </main>
  );
}
