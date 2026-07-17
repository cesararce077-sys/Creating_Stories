package com.creatingstories.workshop.command;

import com.creatingstories.workshop.miapi.OrdinaryToolConverters;
import com.creatingstories.workshop.miapi.ToolCompatibilityPolicy;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TieredItem;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** In-game coverage report; the complete item lists are written to latest.log. */
public final class ModularAuditCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHAT_LIMIT = 30;

    private ModularAuditCommand() {}

    static LiteralArgumentBuilder<CommandSourceStack> node() {
        return Commands.literal("modular")
            .then(Commands.literal("audit")
                .executes(context -> report(context.getSource(), null))
                .then(Commands.literal("converted").executes(context -> report(context.getSource(), Bucket.CONVERTED)))
                .then(Commands.literal("unmapped").executes(context -> report(context.getSource(), Bucket.UNMAPPED)))
                .then(Commands.literal("special").executes(context -> report(context.getSource(), Bucket.SPECIAL)))
                .then(Commands.literal("excluded").executes(context -> report(context.getSource(), Bucket.EXCLUDED))));
    }

    private static int report(CommandSourceStack source, Bucket requested) {
        Map<Bucket, List<ResourceLocation>> results = scan();
        if (requested == null) {
            source.sendSuccess(() -> Component.literal("Modular tool audit: "
                + results.get(Bucket.CONVERTED).size() + " converted, "
                + results.get(Bucket.UNMAPPED).size() + " ordinary unmapped, "
                + results.get(Bucket.SPECIAL).size() + " special/curation, "
                + results.get(Bucket.EXCLUDED).size() + " excluded relics."), false);
            source.sendSuccess(() -> Component.literal(
                "Use /creatingstories modular audit <converted|unmapped|special|excluded>. Full lists are in latest.log."), false);
            results.forEach((bucket, ids) -> LOGGER.info("Creating Stories modular audit {} ({}): {}", bucket, ids.size(), ids));
            return results.values().stream().mapToInt(List::size).sum();
        }

        List<ResourceLocation> ids = results.get(requested);
        source.sendSuccess(() -> Component.literal(requested + " (" + ids.size() + "):"), false);
        ids.stream().limit(CHAT_LIMIT).forEach(id -> source.sendSuccess(() -> Component.literal("  " + id), false));
        if (ids.size() > CHAT_LIMIT) {
            source.sendSuccess(() -> Component.literal("  ... " + (ids.size() - CHAT_LIMIT) + " more; see latest.log"), false);
        }
        LOGGER.info("Creating Stories modular audit {} ({}): {}", requested, ids.size(), ids);
        return ids.size();
    }

    private static Map<Bucket, List<ResourceLocation>> scan() {
        Map<Bucket, List<ResourceLocation>> result = new EnumMap<>(Bucket.class);
        for (Bucket bucket : Bucket.values()) result.put(bucket, new ArrayList<>());
        BuiltInRegistries.ITEM.forEach(item -> {
            if (!(item instanceof TieredItem)) return;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (OrdinaryToolConverters.hasConverter(id)) {
                result.get(Bucket.CONVERTED).add(id);
                return;
            }
            ToolCompatibilityPolicy.Classification classification = ToolCompatibilityPolicy.classify(id.getNamespace(), id.getPath());
            Bucket bucket = switch (classification) {
                case ORDINARY -> Bucket.UNMAPPED;
                case SPECIAL -> Bucket.SPECIAL;
                case EXCLUDED -> Bucket.EXCLUDED;
            };
            result.get(bucket).add(id);
        });
        result.values().forEach(ids -> ids.sort(ResourceLocation::compareTo));
        return result;
    }

    private enum Bucket { CONVERTED, UNMAPPED, SPECIAL, EXCLUDED }
}
