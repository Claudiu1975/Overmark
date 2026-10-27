import { createFileRoute } from "@tanstack/react-router";
import { CoffeeDeck } from "@/components/overmark/coffee-deck";

export const Route = createFileRoute("/coffee")({
  component: CoffeeDeck,
  head: () => ({
    meta: [
      { title: "Buy me a coffee · Overmark" },
      {
        name: "description",
        content:
          "A short presentation of Overmark by Claudiu Florian Ivan, and the text for a Buy Me a Coffee page.",
      },
    ],
  }),
});
