package dev.reliclootcurator.config;

import java.util.List;
import java.util.Set;

public record CuratorConfig(
        double themedChance,
        double fallbackChance,
        List<String> fallbackDimensions,
        List<String> fallbackTablePatterns,
        List<PoolEntry> fallbackPool,
        Set<String> fallbackExclusions,
        Set<String> suppressedDefaultSources
) {
    public CuratorConfig {
        fallbackDimensions = List.copyOf(fallbackDimensions);
        fallbackTablePatterns = List.copyOf(fallbackTablePatterns);
        fallbackPool = List.copyOf(fallbackPool);
        fallbackExclusions = Set.copyOf(fallbackExclusions);
        suppressedDefaultSources = Set.copyOf(suppressedDefaultSources);
    }

    public record PoolEntry(String item, String tag, int weight) {
        public boolean isTag() {
            return tag != null;
        }
    }
}
