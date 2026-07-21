# Relic Loot Curator

Relic Loot Curator is a server-side NeoForge companion for Relics 0.12.8 and artifact addons on Minecraft 1.21.1. It replaces Relics' single global chest roll with two independently configurable stages:

1. matching themed sources receive the normal themed roll;
2. if no themed relic was awarded, an optional low-chance fallback pool is rolled.

The pool and its routing rules live in `config/relic_loot_curator.json`. Fallback sources may name individual item IDs or item tags, allowing Artifacts, Reliquified addons, and future compatible addons to participate without Java changes. Items may retain their normal themed sources, while exclusions and suppressed broad Relics sources remain explicit.

The mod does not disable Relics' native global modifier itself. A pack using the curator must disable `relics:relic_loot` through a datapack override, then register `relic_loot_curator:routed_relic_loot`. Creating Stories provides that override under `kubejs/data/`.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1+
- Relics 0.12.8+
- OctoLib 0.6.2+

## License

MIT
