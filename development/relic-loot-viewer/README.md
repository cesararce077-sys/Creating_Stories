# Relic Loot Viewer

Relic Loot Viewer is a client-side NeoForge integration for Minecraft 1.21.1. It adds a **Relic & Artifact Sources** category to JEI-compatible recipe viewers for every Relics item and every additional artifact configured by Relic Loot Curator.

Relics distributes items through a custom global loot modifier, so general loot-table viewers cannot discover those sources. This mod reads the active Relics loot template instead, including relics registered by addon mods.

The category displays:

- configured loot-table IDs or patterns;
- dimension and biome restrictions;
- relative selection weight;
- a very-low to very-high priority classification calculated from the active relic weight distribution;
- the global Relics generation roll;
- an explicit warning for relics with no positive-weight loot source.

Raw regular expressions are converted into player-facing descriptions such as “Village or pillager structure chests” and “Cold, icy, or snowy biomes.” Unknown addon patterns receive a readable keyword-based fallback rather than exposing regex syntax.

The exact final probability can depend on the combined weights of all relics eligible for a particular loot context.

When Relic Loot Curator is installed, the viewer replaces suppressed broad sources with the curator's actual themed and lucky-fallback routes. It also displays fallback routes for ordinary Artifacts items that do not implement Relics' API. The displayed percentage identifies whether the source uses the normal themed roll or the separate fallback roll.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1+
- Relics 0.12.8+
- JEI 19.20+

EMI can display the category when its JEI compatibility layer is active.

## License

MIT
