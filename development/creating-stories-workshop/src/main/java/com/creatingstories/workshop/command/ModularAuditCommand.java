package com.creatingstories.workshop.command;

import com.creatingstories.workshop.miapi.EquipmentCompatibilityPolicy;
import com.creatingstories.workshop.miapi.ToolCompatibilityPolicy;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import org.slf4j.Logger;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.ModularItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** In-game equipment coverage report; complete lists are written to latest.log. */
public final class ModularAuditCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHAT_LIMIT = 30;

    private ModularAuditCommand() {}

    static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("modular")
            .then(Commands.literal("audit")
                .executes(context -> report(context.getSource(), null, null))
                .then(Commands.literal("converted").executes(context ->
                    report(context.getSource(), Bucket.CONVERTED, null)))
                .then(Commands.literal("unmapped").executes(context ->
                    report(context.getSource(), Bucket.UNMAPPED, null)))
                .then(Commands.literal("special").executes(context ->
                    report(context.getSource(), Bucket.SPECIAL, null)))
                .then(Commands.literal("excluded").executes(context ->
                    report(context.getSource(), Bucket.EXCLUDED, null)))
                .then(Commands.literal("tools").executes(context ->
                    report(context.getSource(), null, Kind.TOOL)))
                .then(Commands.literal("archery").executes(context ->
                    report(context.getSource(), null, Kind.ARCHERY)))
                .then(Commands.literal("armor").executes(context ->
                    report(context.getSource(), null, Kind.ARMOR))));
    }

    private static int report(CommandSourceStack source, Bucket requested, Kind filter) {
        Map<Bucket, List<Entry>> results = scan(filter);
        String scope = filter == null ? "equipment" : filter.label;
        if (requested == null) {
            source.sendSuccess(() -> Component.literal("Modular " + scope + " audit: "
                + results.get(Bucket.CONVERTED).size() + " converted, "
                + results.get(Bucket.UNMAPPED).size() + " ordinary unmapped, "
                + results.get(Bucket.SPECIAL).size() + " special/curation, "
                + results.get(Bucket.EXCLUDED).size() + " protected signature items."), false);
            source.sendSuccess(() -> Component.literal(
                "Use /creatingstories modular audit <tools|archery|armor|converted|unmapped|special|excluded>. Full lists are in latest.log."), false);
            results.forEach((bucket, entries) -> LOGGER.info(
                "Creating Stories modular {} audit {} ({}): {}", scope, bucket, entries.size(), entries));
            return results.values().stream().mapToInt(List::size).sum();
        }

        List<Entry> entries = results.get(requested);
        source.sendSuccess(() -> Component.literal(requested + " (" + entries.size() + "):"), false);
        entries.stream().limit(CHAT_LIMIT).forEach(entry -> source.sendSuccess(() ->
            Component.literal("  [" + entry.kind.label + "] " + entry.id), false));
        if (entries.size() > CHAT_LIMIT) {
            source.sendSuccess(() -> Component.literal(
                "  ... " + (entries.size() - CHAT_LIMIT) + " more; see latest.log"), false);
        }
        LOGGER.info("Creating Stories modular audit {} ({}): {}", requested, entries.size(), entries);
        return entries.size();
    }

    private static Map<Bucket, List<Entry>> scan(Kind filter) {
        Map<Bucket, List<Entry>> result = new EnumMap<>(Bucket.class);
        for (Bucket bucket : Bucket.values()) result.put(bucket, new ArrayList<>());
        BuiltInRegistries.ITEM.forEach(item -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            Kind kind = kind(item, id);
            if (kind == null || filter != null && kind != filter) return;
            ItemStack original = new ItemStack(item);
            if (ModularItem.isModularItem(original)) return;

            ItemStack converted = ModularItemStackConverter.getModularVersion(original);
            if (ModularItem.isModularItem(converted)) {
                result.get(Bucket.CONVERTED).add(new Entry(id, kind));
                return;
            }

            Bucket bucket = switch (classification(id, kind)) {
                case ORDINARY -> Bucket.UNMAPPED;
                case SPECIAL -> Bucket.SPECIAL;
                case EXCLUDED -> Bucket.EXCLUDED;
            };
            result.get(bucket).add(new Entry(id, kind));
        });
        result.values().forEach(entries -> entries.sort((left, right) -> left.id.compareTo(right.id)));
        return result;
    }

    private static Kind kind(Item item, ResourceLocation id) {
        if (item instanceof TieredItem) return Kind.TOOL;
        if (item instanceof ArmorItem || id.equals(ResourceLocation.withDefaultNamespace("elytra"))) return Kind.ARMOR;
        if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof ArrowItem) return Kind.ARCHERY;
        if (EquipmentCompatibilityPolicy.isOrdinary(
            id.getNamespace(), id.getPath(), EquipmentCompatibilityPolicy.Kind.RANGED)) return Kind.ARCHERY;
        return null;
    }

    private static Classification classification(ResourceLocation id, Kind kind) {
        if (kind == Kind.TOOL) {
            return switch (ToolCompatibilityPolicy.classify(id.getNamespace(), id.getPath())) {
                case ORDINARY -> Classification.ORDINARY;
                case SPECIAL -> Classification.SPECIAL;
                case EXCLUDED -> Classification.EXCLUDED;
            };
        }
        EquipmentCompatibilityPolicy.Kind equipmentKind = kind == Kind.ARMOR
            ? EquipmentCompatibilityPolicy.Kind.ARMOR
            : EquipmentCompatibilityPolicy.Kind.RANGED;
        return switch (EquipmentCompatibilityPolicy.classify(id, equipmentKind)) {
            case ORDINARY -> Classification.ORDINARY;
            case SPECIAL -> Classification.SPECIAL;
            case EXCLUDED -> Classification.EXCLUDED;
        };
    }

    private record Entry(ResourceLocation id, Kind kind) {
        @Override public String toString() {
            return "[" + kind.label + "] " + id;
        }
    }

    private enum Kind {
        TOOL("tool"), ARCHERY("archery"), ARMOR("armor");
        private final String label;
        Kind(String label) { this.label = label; }
    }

    private enum Classification { ORDINARY, SPECIAL, EXCLUDED }
    private enum Bucket { CONVERTED, UNMAPPED, SPECIAL, EXCLUDED }
}
