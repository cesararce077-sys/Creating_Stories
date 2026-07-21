package dev.reliclootcurator.loot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RoutingPolicyTest {
    @Test
    void themedRollWinsBeforeFallback() {
        assertEquals(RoutingPolicy.Decision.THEMED,
                RoutingPolicy.decide(true, 0.20, 0.10, true, 0.0075, 0.0));
    }

    @Test
    void fallbackOnlyRunsAfterThemeMisses() {
        assertEquals(RoutingPolicy.Decision.FALLBACK,
                RoutingPolicy.decide(true, 0.20, 0.50, true, 0.0075, 0.005));
    }

    @Test
    void fallbackCanRunWithoutAThemedSource() {
        assertEquals(RoutingPolicy.Decision.FALLBACK,
                RoutingPolicy.decide(false, 0.20, 0.0, true, 0.0075, 0.005));
    }

    @Test
    void ineligibleContextsProduceNothing() {
        assertEquals(RoutingPolicy.Decision.NONE,
                RoutingPolicy.decide(false, 0.20, 0.0, false, 0.0075, 0.0));
    }
}
