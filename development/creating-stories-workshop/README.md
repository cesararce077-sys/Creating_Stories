# Creating Stories Workshop

Pack-specific NeoForge integration for the persistent modular-equipment progression.

Workshop `0.0.25-dev` extends the established hierarchy-safe salvage and
replacement pipeline to Truly Modular: Archery and Truly Modular: Armory.
Automatic conversion is deliberately limited to mundane material variants;
ability-bearing, boss, relic, spellcaster, powered, and other signature gear
keeps its original item behavior.

Coverage can be checked in game with:

```text
/creatingstories modular audit
/creatingstories modular audit tools
/creatingstories modular audit archery
/creatingstories modular audit armor
/creatingstories modular audit unmapped
```

Complete categorized lists are also written to `latest.log`.

## Fusion command prototype

Hold the persistent modular item in the main hand and a donor modular item in the offhand. A component path is the slash-separated MIAPI slot path from the root.

```text
/creatingstories fusion list
/creatingstories fusion preview <component_path>
/creatingstories fusion apply <component_path>
/creatingstories fusion preview_root <destination_path>
/creatingstories fusion apply_root <destination_path>
```

Run `list` first to print the donor's valid component paths.

The `*_root` forms treat the entire offhand item as a standalone component and install its root module into the specified destination. This is the path used to test native MIAPI items such as a Tool Handle left after removing an axe head.

The prototype transfers one exact donor component subtree into the same compatible slot on the persistent item. Applying costs five XP levels and consumes one donor item. It preserves the base stack and its non-MIAPI data components, including its custom name and enchantments.

This command is permission-level 2 while it is a development tool. It is not intended as the final survival interaction.
