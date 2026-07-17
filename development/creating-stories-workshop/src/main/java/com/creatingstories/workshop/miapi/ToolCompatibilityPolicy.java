package com.creatingstories.workshop.miapi;

import java.util.Set;
import java.util.regex.Pattern;

/** Pack policy: automatic conversion is deliberately narrower than MIAPI material discovery. */
public final class ToolCompatibilityPolicy {
    public enum Classification { ORDINARY, SPECIAL, EXCLUDED }

    private static final Set<String> TOOL_SUFFIXES = Set.of("sword", "pickaxe", "axe", "shovel", "hoe");
    private static final Pattern DEEPER_DARKER = Pattern.compile("(?:resonarium|warden)_(sword|pickaxe|axe|shovel|hoe)");
    private static final Pattern ICE_AND_FIRE = Pattern.compile("(?:copper|silver)_(sword|pickaxe|axe|shovel|hoe)");
    private static final Pattern CREATE_SA = Pattern.compile("(?:copper|zinc|brass|rose_quartz)_(sword|pickaxe|axe|shovel|hoe)");
    private static final Pattern CATACLYSM = Pattern.compile("black_steel_(sword|pickaxe|axe|shovel|hoe)");

    private ToolCompatibilityPolicy() {}

    public static Classification classify(String namespace, String path) {
        if (isOrdinary(namespace, path)) return Classification.ORDINARY;
        if (namespace.equals("cataclysm") || namespace.equals("mowziesmobs")
            || namespace.equals("alexscaves") || namespace.equals("irons_spellbooks")) {
            return Classification.EXCLUDED;
        }
        if (namespace.equals("malum") || namespace.equals("iceandfire")
            || namespace.equals("create_sa") || namespace.equals("expanded_combat")) {
            return Classification.SPECIAL;
        }
        return Classification.SPECIAL;
    }

    public static boolean isOrdinary(String namespace, String path) {
        return switch (namespace) {
            case "deeperdarker" -> DEEPER_DARKER.matcher(path).matches();
            case "iceandfire" -> ICE_AND_FIRE.matcher(path).matches();
            case "create_sa" -> CREATE_SA.matcher(path).matches();
            case "cataclysm" -> CATACLYSM.matcher(path).matches();
            default -> false;
        };
    }

    public static String toolType(String path) {
        for (String suffix : TOOL_SUFFIXES) {
            if (path.endsWith("_" + suffix) || path.equals(suffix)) return suffix;
        }
        return "";
    }
}
