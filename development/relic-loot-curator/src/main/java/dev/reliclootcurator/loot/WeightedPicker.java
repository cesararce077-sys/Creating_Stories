package dev.reliclootcurator.loot;

import java.util.List;
import java.util.function.ToIntFunction;

public final class WeightedPicker {
    private WeightedPicker() {
    }

    public static <T> T pick(List<T> entries, ToIntFunction<T> weight, int roll) {
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("Cannot select from an empty pool");
        }

        int remaining = roll;
        for (T entry : entries) {
            int entryWeight = weight.applyAsInt(entry);
            if (entryWeight <= 0) {
                continue;
            }
            remaining -= entryWeight;
            if (remaining < 0) {
                return entry;
            }
        }
        throw new IllegalArgumentException("Roll lies outside the positive pool weight");
    }

    public static <T> int totalWeight(List<T> entries, ToIntFunction<T> weight) {
        long total = 0;
        for (T entry : entries) {
            total += Math.max(0, weight.applyAsInt(entry));
        }
        if (total > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Pool weight exceeds integer range");
        }
        return (int) total;
    }
}
