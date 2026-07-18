package dev.reliclootviewer.jei;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record RelicSourceRecipe(
        ResourceLocation id,
        ItemStack relic,
        List<String> tables,
        List<String> dimensions,
        List<String> biomes,
        int weight,
        WeightClassifier.Band weightBand,
        int minimumWeight,
        int maximumWeight,
        double globalChance,
        boolean obtainable
) {
    public RelicSourceRecipe {
        tables = List.copyOf(tables);
        dimensions = List.copyOf(dimensions);
        biomes = List.copyOf(biomes);
    }
}
