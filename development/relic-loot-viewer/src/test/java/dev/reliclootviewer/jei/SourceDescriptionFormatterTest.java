package dev.reliclootviewer.jei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.Test;

class SourceDescriptionFormatterTest {
    @Test
    void translatesKnownRelicsPatterns() {
        assertEquals("Any generated chest", SourceDescriptionFormatter.format(
                "[\\w]+:chests\\/[\\w_\\/]*[\\w]+[\\w_\\/]*",
                SourceDescriptionFormatter.Kind.LOOT_TABLE));
        assertEquals("Village or pillager structure chests", SourceDescriptionFormatter.format(
                "[\\w]+:chests\\/[\\w_\\/]*(village|pillage)[\\w_\\/]*",
                SourceDescriptionFormatter.Kind.LOOT_TABLE));
        assertEquals("Cold, icy, or snowy biomes", SourceDescriptionFormatter.format(
                "[\\w]+:.*(fro[sz]|ic[ey]|glac|cold|snow)[\\w_\\/]*",
                SourceDescriptionFormatter.Kind.BIOME));
        assertEquals("Any generated structure chest (except the bonus chest)", SourceDescriptionFormatter.format(
                "^(?!minecraft:chests/spawn_bonus_chest$)[a-z0-9_.-]+:chests/.+$",
                SourceDescriptionFormatter.Kind.LOOT_TABLE));
    }

    @Test
    void formatsSpecificLootTables() {
        assertEquals("Ruined Portal chests", SourceDescriptionFormatter.format(
                "minecraft:chests/ruined_portal", SourceDescriptionFormatter.Kind.LOOT_TABLE));
        assertEquals("Overworld", SourceDescriptionFormatter.format(
                "minecraft:overworld", SourceDescriptionFormatter.Kind.DIMENSION));
    }

    @Test
    void neverExposesRegexInFallback() {
        String result = SourceDescriptionFormatter.format(
                "example:chests/.*(tower|crypt).*", SourceDescriptionFormatter.Kind.LOOT_TABLE);
        assertFalse(result.contains(".*"));
        assertFalse(result.contains("["));
    }

    @Test
    void classifiesAgainstConfiguredDistribution() {
        List<Integer> weights = List.of(1, 10, 100, 300, 500);
        assertEquals(WeightClassifier.Band.VERY_LOW, WeightClassifier.classify(1, weights));
        assertEquals(WeightClassifier.Band.TYPICAL, WeightClassifier.classify(100, weights));
        assertEquals(WeightClassifier.Band.VERY_HIGH, WeightClassifier.classify(500, weights));
    }
}
