package dev.reliclootviewer.jei;

import java.util.List;
import java.util.Map;
import java.util.Set;

record CuratorSnapshot(
        boolean active,
        double themedChance,
        double fallbackChance,
        List<String> fallbackDimensions,
        List<String> fallbackTablePatterns,
        Map<String, FallbackEntry> fallbackEntries,
        Set<String> suppressedDefaultSources
) {
    static CuratorSnapshot inactive() {
        return new CuratorSnapshot(false, 0.0, 0.0, List.of(), List.of(), Map.of(), Set.of());
    }

    record FallbackEntry(int weight) {
    }
}
