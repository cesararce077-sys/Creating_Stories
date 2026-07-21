package dev.reliclootcurator.api;

import dev.reliclootcurator.config.CuratorConfigManager;
import dev.reliclootcurator.loot.CuratedRelicRouter;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;

public final class RelicLootCuratorApi {
    private RelicLootCuratorApi() {
    }

    public static double themedChance() {
        return CuratorConfigManager.snapshot().themedChance();
    }

    public static double fallbackChance() {
        return CuratorConfigManager.snapshot().fallbackChance();
    }

    public static List<String> fallbackDimensions() {
        return CuratorConfigManager.snapshot().fallbackDimensions();
    }

    public static List<String> fallbackTablePatterns() {
        return CuratorConfigManager.snapshot().fallbackTablePatterns();
    }

    public static List<FallbackEntry> fallbackEntries() {
        return CuratedRelicRouter.state().fallbackCandidates().stream()
                .map(entry -> new FallbackEntry(
                        BuiltInRegistries.ITEM.getKey(entry.item()).toString(),
                        entry.weight()))
                .toList();
    }

    public static Set<String> suppressedDefaultSources() {
        return CuratorConfigManager.snapshot().suppressedDefaultSources();
    }

    public record FallbackEntry(String item, int weight) {
    }
}
