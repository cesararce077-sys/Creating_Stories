package dev.reliclootcurator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

public final class CuratorConfigManager {
    public static final String FILE_NAME = "relic_loot_curator.json";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile CuratorConfig snapshot;

    private CuratorConfigManager() {
    }

    public static CuratorConfig snapshot() {
        CuratorConfig current = snapshot;
        if (current != null) {
            return current;
        }
        synchronized (CuratorConfigManager.class) {
            if (snapshot == null) {
                snapshot = load(FMLPaths.CONFIGDIR.get().resolve(FILE_NAME));
            }
            return snapshot;
        }
    }

    public static void reload() {
        synchronized (CuratorConfigManager.class) {
            snapshot = load(FMLPaths.CONFIGDIR.get().resolve(FILE_NAME));
        }
    }

    static CuratorConfig load(Path path) {
        if (!Files.isRegularFile(path)) {
            LOGGER.warn("Relic Loot Curator config {} is missing; no fallback relics will be generated", path);
            return emptyConfig();
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            RawConfig raw = GSON.fromJson(reader, RawConfig.class);
            return validate(raw);
        } catch (IOException | JsonParseException exception) {
            LOGGER.error("Unable to read Relic Loot Curator config {}; no fallback relics will be generated", path, exception);
            return emptyConfig();
        }
    }

    static CuratorConfig validate(RawConfig raw) {
        if (raw == null) {
            throw new JsonParseException("Config root must be an object");
        }

        double themedChance = chance(raw.themedChance, "themed_chance");
        double fallbackChance = chance(raw.fallbackChance, "fallback_chance");
        List<String> dimensions = cleanStrings(raw.fallbackDimensions);
        List<String> tablePatterns = cleanPatterns(raw.fallbackTablePatterns);
        List<CuratorConfig.PoolEntry> pool = cleanPool(raw.fallbackPool);
        Set<String> exclusions = new LinkedHashSet<>(cleanStrings(raw.fallbackExclusions));
        Set<String> suppressed = new LinkedHashSet<>(cleanStrings(raw.suppressedDefaultSources));

        return new CuratorConfig(themedChance, fallbackChance, dimensions, tablePatterns, pool, exclusions, suppressed);
    }

    private static double chance(Double value, String key) {
        if (value == null || !Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new JsonParseException(key + " must be between 0 and 1");
        }
        return value;
    }

    private static List<String> cleanStrings(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private static List<String> cleanPatterns(List<String> values) {
        List<String> patterns = cleanStrings(values);
        for (String expression : patterns) {
            try {
                Pattern.compile(expression);
            } catch (PatternSyntaxException exception) {
                throw new JsonParseException("Invalid fallback table pattern: " + expression, exception);
            }
        }
        return patterns;
    }

    private static List<CuratorConfig.PoolEntry> cleanPool(List<RawPoolEntry> values) {
        if (values == null) {
            return List.of();
        }

        List<CuratorConfig.PoolEntry> entries = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (RawPoolEntry value : values) {
            if (value == null) {
                throw new JsonParseException("fallback_pool entries cannot be null");
            }

            String item = cleanNullable(value.item);
            String tag = cleanNullable(value.tag);
            if ((item == null) == (tag == null)) {
                throw new JsonParseException("Every fallback_pool entry requires exactly one of item or tag");
            }
            String source = item != null ? "item:" + item : "tag:" + tag;
            if (value.weight == null || value.weight <= 0) {
                throw new JsonParseException("Fallback weight must be positive for " + source);
            }
            if (!seen.add(source)) {
                throw new JsonParseException("Duplicate fallback source: " + source);
            }
            entries.add(new CuratorConfig.PoolEntry(item, tag, value.weight));
        }
        return entries;
    }

    private static String cleanNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static CuratorConfig emptyConfig() {
        return new CuratorConfig(0.20, 0.0, List.of(), List.of(), List.of(), Set.of(), Set.of());
    }

    static final class RawConfig {
        @com.google.gson.annotations.SerializedName("themed_chance")
        Double themedChance;
        @com.google.gson.annotations.SerializedName("fallback_chance")
        Double fallbackChance;
        @com.google.gson.annotations.SerializedName("fallback_dimensions")
        List<String> fallbackDimensions;
        @com.google.gson.annotations.SerializedName("fallback_table_patterns")
        List<String> fallbackTablePatterns;
        @com.google.gson.annotations.SerializedName("fallback_pool")
        List<RawPoolEntry> fallbackPool;
        @com.google.gson.annotations.SerializedName("fallback_exclusions")
        List<String> fallbackExclusions;
        @com.google.gson.annotations.SerializedName("suppressed_default_sources")
        List<String> suppressedDefaultSources;
    }

    static final class RawPoolEntry {
        String item;
        String tag;
        Integer weight;
    }
}
