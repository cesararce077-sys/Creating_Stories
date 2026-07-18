package dev.reliclootviewer.jei;

import dev.reliclootviewer.RelicLootViewer;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.init.RelicsConfigs;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@JeiPlugin
public final class RelicLootViewerJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(RelicLootViewer.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new RelicSourceCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RelicSourceRecipe> recipes = new ArrayList<>();
        double globalChance = RelicsConfigs.LOOT_CONFIG.getRelicGenChance();
        List<Integer> configuredWeights = collectConfiguredWeights();
        int minimumWeight = configuredWeights.isEmpty() ? 0 : configuredWeights.getFirst();
        int maximumWeight = configuredWeights.isEmpty() ? 0 : configuredWeights.getLast();

        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof IRelicItem relic)) {
                continue;
            }

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            var lootTemplate = relic.getDefaultLootTemplate();
            var entries = lootTemplate == null ? List.<it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry>of()
                    : lootTemplate.getEntries();
            int sourceIndex = 0;

            if (entries != null) {
                for (var entry : entries) {
                    if (entry == null || entry.getWeight() <= 0) {
                        continue;
                    }

                    recipes.add(new RelicSourceRecipe(
                            recipeId(itemId, sourceIndex++),
                            item.getDefaultInstance(),
                            safe(entry.getTables()),
                            safe(entry.getDimensions()),
                            safe(entry.getBiomes()),
                            entry.getWeight(),
                            WeightClassifier.classify(entry.getWeight(), configuredWeights),
                            minimumWeight,
                            maximumWeight,
                            globalChance,
                            true
                    ));
                }
            }

            if (sourceIndex == 0) {
                recipes.add(new RelicSourceRecipe(
                        recipeId(itemId, 0),
                        item.getDefaultInstance(),
                        List.of(), List.of(), List.of(),
                        0,
                        WeightClassifier.Band.TYPICAL,
                        minimumWeight,
                        maximumWeight,
                        globalChance,
                        false
                ));
            }
        }

        registration.addRecipes(RelicSourceCategory.TYPE, recipes);
    }

    private static List<Integer> collectConfiguredWeights() {
        return BuiltInRegistries.ITEM.stream()
                .filter(IRelicItem.class::isInstance)
                .map(IRelicItem.class::cast)
                .map(IRelicItem::getDefaultLootTemplate)
                .filter(java.util.Objects::nonNull)
                .flatMap(template -> template.getEntries() == null
                        ? java.util.stream.Stream.empty()
                        : template.getEntries().stream())
                .filter(java.util.Objects::nonNull)
                .map(entry -> entry.getWeight())
                .filter(weight -> weight > 0)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private static List<String> safe(List<String> values) {
        return values == null ? List.of() : values;
    }

    private static ResourceLocation recipeId(ResourceLocation itemId, int sourceIndex) {
        return ResourceLocation.fromNamespaceAndPath(
                RelicLootViewer.MOD_ID,
                itemId.getNamespace() + "/" + itemId.getPath() + "/" + sourceIndex
        );
    }
}
