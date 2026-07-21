package dev.reliclootviewer.jei;

import dev.reliclootcurator.api.RelicLootCuratorApi;
import java.util.LinkedHashMap;
import java.util.Map;

final class CuratorIntegration {
    private CuratorIntegration() {
    }

    static CuratorSnapshot load() {
        Map<String, CuratorSnapshot.FallbackEntry> entries = new LinkedHashMap<>();
        for (RelicLootCuratorApi.FallbackEntry entry : RelicLootCuratorApi.fallbackEntries()) {
            entries.put(entry.item(), new CuratorSnapshot.FallbackEntry(entry.weight()));
        }
        return new CuratorSnapshot(
                true,
                RelicLootCuratorApi.themedChance(),
                RelicLootCuratorApi.fallbackChance(),
                RelicLootCuratorApi.fallbackDimensions(),
                RelicLootCuratorApi.fallbackTablePatterns(),
                Map.copyOf(entries),
                RelicLootCuratorApi.suppressedDefaultSources()
        );
    }
}
