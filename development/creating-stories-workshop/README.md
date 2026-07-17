# Creating Stories Workshop

Pack-specific NeoForge integration for the persistent modular-equipment progression.

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
