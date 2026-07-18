package dev.reliclootviewer.jei;

import java.util.List;

public final class WeightClassifier {
    private WeightClassifier() {
    }

    public static Band classify(int weight, List<Integer> sortedDistinctWeights) {
        if (sortedDistinctWeights.size() <= 1) {
            return Band.TYPICAL;
        }

        int index = sortedDistinctWeights.indexOf(weight);
        if (index < 0) {
            index = insertionPoint(weight, sortedDistinctWeights);
        }

        double percentile = (double) index / (sortedDistinctWeights.size() - 1);
        if (percentile < 0.20) return Band.VERY_LOW;
        if (percentile < 0.40) return Band.LOW;
        if (percentile <= 0.60) return Band.TYPICAL;
        if (percentile <= 0.80) return Band.HIGH;
        return Band.VERY_HIGH;
    }

    private static int insertionPoint(int weight, List<Integer> weights) {
        int index = 0;
        while (index < weights.size() && weights.get(index) < weight) {
            index++;
        }
        return Math.min(index, weights.size() - 1);
    }

    public enum Band {
        VERY_LOW("jei.relic_loot_viewer.weight.very_low"),
        LOW("jei.relic_loot_viewer.weight.low"),
        TYPICAL("jei.relic_loot_viewer.weight.typical"),
        HIGH("jei.relic_loot_viewer.weight.high"),
        VERY_HIGH("jei.relic_loot_viewer.weight.very_high");

        private final String translationKey;

        Band(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }
}
