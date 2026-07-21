# Creating Stories — Agent Context

## Project

- This repository is the working CurseForge instance for **Creating Stories**, a Minecraft 1.21.1 NeoForge modpack centered on exploration, staged progression, Create: Aeronautics, and integrated magic.
- Loader: NeoForge 21.1.235.
- Treat this directory as both a Git repository and a live game instance. Runtime content can be large or generated.
- Read the documents under `Design/` when working on progression, interactions, balance, quests, or the roadmap. `Design/` is intentionally ignored by Git because it is private agent/design context.

## Repository Conventions

- Preserve unrelated user changes. The worktree may already be dirty.
- Do not add runtime worlds, logs, caches, downloaded mod JARs, or the private `Design/` directory to Git.
- `mods/` is intentionally ignored; release distribution is represented through `manifest.json`, not by committing third-party JARs.
- Prefer `rg`/`rg --files` for searches and `apply_patch` for hand-authored edits.
- Before replacing a mod JAR, verify Minecraft/Java is not running. Keep exactly one loadable JAR for each mod ID.
- Back up replaced local JARs outside `mods/` so NeoForge cannot discover duplicate mod IDs.
- Do not commit, tag, push, or publish unless the user explicitly asks.

## Unified Magic Design

The pack requires one coherent casting/progression experience across Ars Nouveau and Iron's Spells 'n Spellbooks. Separate spellbooks are not an acceptable final design.

Current integration versions:

- Ars Nouveau 5.12.1
- Iron's Spells 'n Spellbooks 3.16.2
- Creating Stories — Unified Magic Integration fork `3.0.2-creatingstories.4`

The installed fork is:

`mods/creating_stories_unified_magic_integration-3.0.2-creatingstories.4.jar`

Its source checkout is:

`development/creating-stories-unified-magic-integration/`

That directory is a nested Git checkout based on upstream branch `port/neoforge-1.21.1`, using local branch `pack-compat-1.21.1`. The important pack fix is in:

`development/creating-stories-unified-magic-integration/src/main/java/com/otectus/arsnspells/spell/irons/ArsCrossProxySpell.java`

Iron's native-wheel proxy casts may provide an empty `MagicData.getPlayerCastingItem()`, with both player hands empty, because the active book lives in Curios slot `spellbook:0`. The fork resolves candidates in this order:

1. Iron's recorded casting item.
2. Main hand.
3. Offhand.
4. `io.redspace.ironsspellbooks.api.util.Utils.getPlayerSpellbookStack(player)`.

Only a candidate carrying the matching Ars proxy payload is accepted. Do not remove the equipped-spellbook fallback.

The `.4` fork also fixes Spellbook Binding ritual completion. One-shot Ars rituals must call `setFinished()` from `tick()`; otherwise Ars never invokes `onEnd()` and the brazier appears to run forever without binding the valid Loom-created Ars Spell Scroll.

Mana is unified successfully. The redundant Iron's client mana bar is hidden through `config/irons_spellbooks-client.toml` (`manaBarDisplay = "Never"`), leaving one visible mana bar.

## Modular Equipment Integration

The pack-owned addon source is in `development/creating-stories-workshop/`. Its installed runtime JAR is kept in `mods/`, while the release copy belongs in `overrides/mods/` because it is not a third-party CurseForge manifest dependency.

Workshop `0.0.9-dev` currently provides hierarchy-safe MIAPI part salvage/replacement, ordinary tool converters, pack-owned zinc/brass/rose-quartz materials, a modular compatibility audit command, and canonical copper crafting. Both Ice and Fire and Create Stuff & Additions copper tools convert to `miapi:metal/copper`, but only Ice and Fire's copper tool recipes remain enabled to prevent ambiguous autocrafting outputs.

Build the addon from `development/creating-stories-workshop` with the Java 21 JDK and Gradle wrapper stored in the unified-magic checkout. Preserve only one loadable Workshop JAR in `mods/`, and back up replaced builds under the addon's ignored `backups/` directory.

## Relic Loot Integration

Relics' native modifier uses one global chest chance before weighted selection. A low item weight does not make a generic relic rare when no themed candidate is present. The pack therefore uses the reusable addon source at `development/relic-loot-curator/`.

Installed integration versions:

- Relic Loot Curator `0.2.0`
- Relic Loot Viewer `0.3.0`

The curator preserves the normal 20% attempt for matching themed Relics entries. Only when that attempt produces nothing does it roll the 2% Overworld fallback pool from `config/relic_loot_curator.json`. The explicit pool is audited from the `artifacts:artifacts` and `reliquified_artifacts:mimic_loot` item tags, covering Artifacts, base Relics, Reliquified Ars Nouveau, and Reliquified Iron's. The curator also supports tag entries for other packs, but Creating Stories materializes the current 108 IDs so the JEI viewer can show them before a world supplies synchronized item tags. It never adds a fallback when generated loot already contains a configured artifact or any Relics item. Experience Disperser and Roller Skate have their overly broad native sources suppressed; other fallback members retain their themed sources.

`kubejs/data/relics/loot_modifiers/relic_loot.json` disables Relics' native modifier so the curator is the sole full-relic selector. Do not remove that override while the curator is installed. Relic Loot Viewer reads the curator API and displays the actual themed or lucky-fallback route in JEI.

Build the curator before the viewer because the viewer has an optional compile-time integration with its API. Use Java 21 and each project's Gradle wrapper. Runtime JARs belong in both `mods/` and `overrides/mods/`; keep only one loadable version of each mod ID.

## Archaeology Viewer Integration

The pack-owned Just Enough Archaeology addon source is in `development/just-enough-archaeology-integrations/`. Version `0.1.2` adds the missing IDAS and Integrated Villages brush-table mappings and fixes Just Enough Archaeology 1.2.0 retaining generated recipe displays across integrated-server instances.

Do not remove `HelperRecipeCacheMixin`. Just Enough Archaeology's static brushing/sniffing caches can retain Ancient Book item stacks backed by a previous world's enchantment registry. Re-entering or opening another world in the same client then disconnects with `Failed to encode packet 'clientbound/minecraft:update_recipes'` and `Can't find id` for an enchantment holder. The mixin clears each generated cache before the addon rebuilds it for the current server. Runtime JARs belong in both `mods/` and `overrides/mods/`; keep only one loadable version.

## Building the Ars 'n Spells Fork

Minecraft 1.21.1 requires a full Java 21 JDK. CurseForge's `Jre_21` is runtime-only and cannot perform the NeoForge recompile. A project-local Temurin JDK is kept under the fork's ignored `.jdk21/` directory.

From `development/creating-stories-unified-magic-integration` in PowerShell:

```powershell
$env:JAVA_HOME=(Get-ChildItem .jdk21 -Directory | Select-Object -First 1).FullName
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat clean test build --console=plain
```

Expected artifact:

`development/creating-stories-unified-magic-integration/build/libs/creating_stories_unified_magic_integration-3.0.2-creatingstories.4.jar`

When changing the fork, increment the `-creatingstories.N` suffix in its `gradle.properties`, run the complete test/build, verify the artifact metadata/hash, close Minecraft, move the prior installed fork into `development/creating-stories-unified-magic-integration/backups/`, and copy only the new runtime JAR into `mods/`.

## Unified-Casting Regression Test

Use a highly visible Ars spell such as:

`Projectile → Split → Split → Split → Harm`

Bind it to an Iron's spellbook, equip the book in the Iron's/Curios spellbook slot, select the proxy spell using Iron's native wheel, and cast with empty hands. Success requires visible Ars projectiles and actual damage—not merely Iron's sound or hand animation.

Also verify:

- Native Iron's spells still cast.
- Mana is charged once from the unified pool.
- Only one mana bar is visible.
- The spell still works after relogging and after equipping the book again.
- Both main-hand and offhand books remain safe fallbacks.

## Known Integration Issue

Putting an Ars 'n Spells Loom carrier scroll into Iron's inscription table and clicking Inscribe can crash in Iron's `InscriptionTableScreen.onInscription` because its scroll container is null. Normal progression should use the Ars 'n Spells spellbook-binding ritual, but invalid input must eventually be rejected safely rather than crash. This safety patch remains outstanding unless later work has explicitly resolved and tested it.

## Release and Roadmap

- Current pack version is 0.3.1; verify the current manifest/tag before preparing another release.
- Every release must update `config/modpack-update-checker/config.json`, set `latestVersion` in `update/meta.json` to the same version, append the release entry under `update/meta.json`, and add `update/versions/<version>/changelog.txt`. Never leave `latestVersion` implicit: Modpack Update Checker's fallback inference can identify an older entry as latest, and its toast treats any unequal version as an available update. Keep the raw GitHub update base URL stable so older installations can discover later releases.
- Roadmap work must start by reading `Design/` and reconciling it with the mods actually present in `manifest.json`/`mods/`.
- Favor realistic, testable player interactions and balance milestones over broad feature lists.
