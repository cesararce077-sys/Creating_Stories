package dev.reliclootcurator.loot;

public final class RoutingPolicy {
    private RoutingPolicy() {
    }

    public static Decision decide(
            boolean hasThemedCandidates,
            double themedChance,
            double themedRoll,
            boolean fallbackEligible,
            double fallbackChance,
            double fallbackRoll
    ) {
        if (hasThemedCandidates && themedRoll < themedChance) {
            return Decision.THEMED;
        }
        if (fallbackEligible && fallbackRoll < fallbackChance) {
            return Decision.FALLBACK;
        }
        return Decision.NONE;
    }

    public enum Decision {
        THEMED,
        FALLBACK,
        NONE
    }
}
