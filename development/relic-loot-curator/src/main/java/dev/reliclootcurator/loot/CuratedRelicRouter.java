package dev.reliclootcurator.loot;

import com.mojang.logging.LogUtils;
import dev.reliclootcurator.config.CuratorConfig;
import dev.reliclootcurator.config.CuratorConfigManager;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

public final class CuratedRelicRouter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile RouterState state;

    private CuratedRelicRouter() {
    }

    public static RouterState state() {
        RouterState current = state;
        if (current != null) {
            return current;
        }
        synchronized (CuratedRelicRouter.class) {
            if (state == null) {
                state = build(CuratorConfigManager.snapshot());
            }
            return state;
        }
    }

    public static void rebuild() {
        synchronized (CuratedRelicRouter.class) {
            state = build(CuratorConfigManager.snapshot());
        }
    }

    public static void clear() {
        state = null;
    }

    private static RouterState build(CuratorConfig config) {
        Set<String> suppressed = config.suppressedDefaultSources();
        List<ThemedCandidate> themed = new ArrayList<>();
        Map<Item, Integer> resolvedFallback = new LinkedHashMap<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof IRelicItem relic)) {
                continue;
            }

            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (suppressed.contains(id.toString())) {
                continue;
            }

            var template = relic.getDefaultLootTemplate();
            if (template == null || template.getEntries() == null) {
                continue;
            }
            for (LootEntry entry : template.getEntries()) {
                if (entry == null || entry.getWeight() <= 0) {
                    continue;
                }
                try {
                    themed.add(new ThemedCandidate(
                            item,
                            entry.getWeight(),
                            compile(entry.getDimensions()),
                            compile(entry.getBiomes()),
                            compile(entry.getTables())
                    ));
                } catch (PatternSyntaxException exception) {
                    LOGGER.error("Skipping invalid Relics loot source for {}", id, exception);
                }
            }
        }

        for (CuratorConfig.PoolEntry entry : config.fallbackPool()) {
            if (entry.isTag()) {
                addFallbackTag(resolvedFallback, entry.tag(), entry.weight(), config.fallbackExclusions());
            } else {
                addFallbackItem(resolvedFallback, entry.item(), entry.weight(), config.fallbackExclusions());
            }
        }
        List<FallbackCandidate> fallback = resolvedFallback.entrySet().stream()
                .map(entry -> new FallbackCandidate(entry.getKey(), entry.getValue()))
                .toList();

        RouterState result = new RouterState(
                config.themedChance(),
                config.fallbackChance(),
                List.copyOf(themed),
                List.copyOf(fallback),
                Set.copyOf(resolvedFallback.keySet()),
                compile(config.fallbackDimensions()),
                compile(config.fallbackTablePatterns())
        );
        LOGGER.info(
                "Relic Loot Curator loaded {} themed entries and {} fallback relic/artifact items (themed={}%, fallback={}%)",
                themed.size(), fallback.size(), config.themedChance() * 100.0, config.fallbackChance() * 100.0
        );
        return result;
    }

    private static void addFallbackTag(Map<Item, Integer> resolved,
                                       String tagName,
                                       int weight,
                                       Set<String> exclusions) {
        ResourceLocation id = ResourceLocation.tryParse(tagName);
        if (id == null) {
            LOGGER.error("Skipping invalid fallback item tag {}", tagName);
            return;
        }
        TagKey<Item> tag = TagKey.create(Registries.ITEM, id);
        var values = BuiltInRegistries.ITEM.getTag(tag);
        if (values.isEmpty()) {
            LOGGER.error("Skipping missing or empty fallback item tag #{}", id);
            return;
        }
        values.get().forEach(holder -> addResolvedFallback(resolved, holder.value(), weight, exclusions));
    }

    private static void addFallbackItem(Map<Item, Integer> resolved,
                                        String itemName,
                                        int weight,
                                        Set<String> exclusions) {
        ResourceLocation id = ResourceLocation.tryParse(itemName);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            LOGGER.error("Skipping missing fallback item {}", itemName);
            return;
        }
        addResolvedFallback(resolved, BuiltInRegistries.ITEM.get(id), weight, exclusions);
    }

    private static void addResolvedFallback(Map<Item, Integer> resolved,
                                            Item item,
                                            int weight,
                                            Set<String> exclusions) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        if (item == Items.AIR || exclusions.contains(id.toString())) {
            return;
        }
        Integer existing = resolved.putIfAbsent(item, weight);
        if (existing != null && existing != weight) {
            LOGGER.warn("Fallback item {} was resolved more than once with weights {} and {}; keeping {}",
                    id, existing, weight, existing);
        }
    }

    private static List<Pattern> compile(List<String> expressions) {
        if (expressions == null) {
            return List.of();
        }
        return expressions.stream().map(Pattern::compile).toList();
    }

    public record RouterState(
            double themedChance,
            double fallbackChance,
            List<ThemedCandidate> themedCandidates,
            List<FallbackCandidate> fallbackCandidates,
            Set<Item> fallbackItems,
            List<Pattern> fallbackDimensions,
            List<Pattern> fallbackTables
    ) {
        public List<ThemedCandidate> matchingThemed(String dimension, String biome, String table) {
            return themedCandidates.stream()
                    .filter(candidate -> candidate.matches(dimension, biome, table))
                    .toList();
        }

        public boolean fallbackEligible(String dimension, String table) {
            return matchesAny(fallbackDimensions, dimension)
                    && matchesAny(fallbackTables, table)
                    && !fallbackCandidates.isEmpty();
        }

        public boolean isCuratedLootItem(Item item) {
            return item instanceof IRelicItem || fallbackItems.contains(item);
        }

        public Item chooseThemed(List<ThemedCandidate> candidates, RandomSource random) {
            int total = WeightedPicker.totalWeight(candidates, ThemedCandidate::weight);
            return WeightedPicker.pick(candidates, ThemedCandidate::weight, random.nextInt(total)).item();
        }

        public Item chooseFallback(RandomSource random) {
            int total = WeightedPicker.totalWeight(fallbackCandidates, FallbackCandidate::weight);
            return WeightedPicker.pick(fallbackCandidates, FallbackCandidate::weight, random.nextInt(total)).item();
        }
    }

    public record ThemedCandidate(
            Item item,
            int weight,
            List<Pattern> dimensions,
            List<Pattern> biomes,
            List<Pattern> tables
    ) {
        boolean matches(String dimension, String biome, String table) {
            return matchesAny(dimensions, dimension)
                    && matchesAny(biomes, biome)
                    && matchesAny(tables, table);
        }
    }

    public record FallbackCandidate(Item item, int weight) {
    }

    private static boolean matchesAny(List<Pattern> patterns, String value) {
        return patterns.stream().anyMatch(pattern -> pattern.matcher(value).matches());
    }
}
