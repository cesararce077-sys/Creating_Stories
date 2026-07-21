package dev.reliclootviewer.jei;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SourceDescriptionFormatter {
    private static final Pattern ALTERNATIVES = Pattern.compile("\\(([^()]+\\|[^()]+)+\\)");
    private static final Pattern WORDS = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]{2,}");

    private static final Map<String, String> EXACT = Map.ofEntries(
            Map.entry(".*", "Any"),
            Map.entry("minecraft:overworld", "Overworld"),
            Map.entry("minecraft:the_nether", "The Nether"),
            Map.entry("minecraft:the_end", "The End"),
            Map.entry("minecraft:deep_dark", "Deep Dark"),
            Map.entry("minecraft:chests/ruined_portal", "Ruined Portal chests"),
            Map.entry("^(?!minecraft:chests/spawn_bonus_chest$)[a-z0-9_.-]+:chests/.+$", "Any generated structure chest (except the bonus chest)"),
            Map.entry("[\\w]+:chests\\/[\\w_\\/]*[\\w]+[\\w_\\/]*", "Any generated chest"),
            Map.entry("[\\w]+:chests\\/[\\w_\\/]*(village|pillage)[\\w_\\/]*", "Village or pillager structure chests"),
            Map.entry("[\\w]+:chests\\/[\\w_\\/]*(bastion|piglin)[\\w_\\/]*", "Bastion or piglin structure chests"),
            Map.entry("[\\w]+:chests\\/[\\w_\\/]*(mine)[\\w_\\/]*", "Mineshaft or mining structure chests"),
            Map.entry("[\\w]+:chests\\/[\\w_\\/]*(end|stronghold)[\\w_\\/]*", "End or stronghold structure chests"),
            Map.entry("[\\w]+:.*(desert|badlands|outback)[\\w_\\/]*", "Desert or badlands biomes"),
            Map.entry("[\\w]+:.*(savanna|steppe|prairie)[\\w_\\/]*", "Savanna or steppe biomes"),
            Map.entry("[\\w]+:.*(forest|wood|timberland|silva|wildwood|garden|grove)[\\w_\\/]*", "Forest or woodland biomes"),
            Map.entry("[\\w]+:.*(mountain|peak|summit|ridge|alp|highland|hill|cliff|height)[\\w_\\/]*", "Mountain or highland biomes"),
            Map.entry("[\\w]+:.*(jungle|rainforest|tropic|wildwood|thicket|boscage|humid|bamboo)[\\w_\\/]*", "Jungle or tropical biomes"),
            Map.entry("[\\w]+:.*(taiga|pine)[\\w_\\/]*", "Taiga or pine biomes"),
            Map.entry("[\\w]+:.*(plain|fiel|prairie|steppe|meadow|flat|grass|bush)[\\w_\\/]*", "Plains, meadow, or grassland biomes"),
            Map.entry("[\\w]+:.*(swamp|marsh|bog|fen|wetland|quagmire|morass|slough|bayou|mud)[\\w_\\/]*", "Swamp or wetland biomes"),
            Map.entry("[\\w]+:.*(fro[sz]|ic[ey]|glac|cold|snow)[\\w_\\/]*", "Cold, icy, or snowy biomes"),
            Map.entry("[\\w]+:.*(cave|cavern|grotto|hollow|den|chamber|crypt|subterranean)[\\w_\\/]*", "Cave or underground biomes"),
            Map.entry("[\\w]+:.*(sculk|warden)[\\w_\\/]*", "Sculk or Warden-related biomes")
    );

    private static final String NETHER_PATTERN =
            "[\\w]+:chests\\/[\\w_\\/]*(nether|infern|hell|chasm|lava|magma|m[eo]lt|fire|flame|blaze|ember|pyre)[\\w_\\/]*";
    private static final String AQUATIC_PATTERN =
            "[\\w]+:.*(ocean|sea|marine|pelagic|beach|shore|coast|strand|sandbank|river|stream|creek|brook|water|tributary)[\\w_\\/]*";

    private SourceDescriptionFormatter() {
    }

    public static String formatAll(List<String> values, Kind kind) {
        List<String> formatted = values.stream()
                .map(value -> format(value, kind))
                .distinct()
                .toList();
        return String.join("; ", formatted);
    }

    public static String format(String expression, Kind kind) {
        if (expression == null || expression.isBlank()) {
            return "Any";
        }
        String exact = EXACT.get(expression);
        if (exact != null) {
            return exact;
        }
        if (NETHER_PATTERN.equals(expression)) {
            return "Nether or fire-themed structure chests";
        }
        if (AQUATIC_PATTERN.equals(expression)) {
            return "Ocean, river, beach, or other aquatic biomes";
        }
        if (looksLikeResourceLocation(expression)) {
            return friendlyResourceLocation(expression, kind);
        }

        Set<String> alternatives = extractAlternatives(expression);
        if (!alternatives.isEmpty()) {
            String names = alternatives.stream().map(SourceDescriptionFormatter::titleCase).reduce((a, b) -> a + ", " + b).orElse("");
            return switch (kind) {
                case LOOT_TABLE -> "Structure chests with names containing: " + names;
                case BIOME -> "Biomes with names containing: " + names;
                case DIMENSION -> "Dimensions with names containing: " + names;
            };
        }

        Set<String> keywords = extractKeywords(expression);
        String suffix = keywords.isEmpty() ? "custom configured pattern" : String.join(", ", keywords);
        return switch (kind) {
            case LOOT_TABLE -> "Matching structure chests (" + suffix + ")";
            case BIOME -> "Matching biomes (" + suffix + ")";
            case DIMENSION -> "Matching dimensions (" + suffix + ")";
        };
    }

    private static boolean looksLikeResourceLocation(String value) {
        return value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }

    private static String friendlyResourceLocation(String value, Kind kind) {
        String[] halves = value.split(":", 2);
        String path = halves[1].replaceFirst("^(chests|gameplay)/", "");
        String name = titleCase(path.replace('/', ' '));
        String namespace = "minecraft".equals(halves[0]) ? "" : " (" + titleCase(halves[0]) + ")";
        if (kind == Kind.LOOT_TABLE && !name.toLowerCase(Locale.ROOT).contains("chest")) {
            name += " chests";
        }
        return name + namespace;
    }

    private static Set<String> extractAlternatives(String expression) {
        Matcher matcher = ALTERNATIVES.matcher(expression);
        Set<String> values = new LinkedHashSet<>();
        while (matcher.find()) {
            for (String value : matcher.group(1).split("\\|")) {
                String cleaned = value.replaceAll("[^a-zA-Z0-9_ -]", "");
                if (!cleaned.isBlank() && !cleaned.contains("[")) {
                    values.add(cleaned);
                }
            }
        }
        return values;
    }

    private static Set<String> extractKeywords(String expression) {
        Matcher matcher = WORDS.matcher(expression);
        Set<String> ignored = Set.of("chests", "minecraft", "gameplay");
        Set<String> words = new LinkedHashSet<>();
        while (matcher.find() && words.size() < 8) {
            String word = matcher.group().toLowerCase(Locale.ROOT);
            if (!ignored.contains(word)) {
                words.add(titleCase(word));
            }
        }
        return words;
    }

    static String titleCase(String input) {
        List<String> words = new ArrayList<>();
        for (String word : input.replace('_', ' ').trim().split("\\s+")) {
            if (!word.isBlank()) {
                words.add(Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return String.join(" ", words);
    }

    public enum Kind {
        LOOT_TABLE,
        DIMENSION,
        BIOME
    }
}
