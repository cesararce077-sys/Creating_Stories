package com.creatingstories.workshop.miapi;

import java.util.List;
import java.util.Locale;

final class SalvagedPartNameRules {
    private SalvagedPartNameRules() {}

    static String partName(String moduleId) {
        String path = path(moduleId);
        if (path.contains("handle/sword")) return "Sword Handle";
        if (path.contains("handle/tool")) return "Tool Handle";
        if (path.contains("handle/polearm")) return "Polearm Handle";
        if (path.contains("handle/")) return title(last(path)) + " Handle";
        if (path.contains("blade/normal") || path.contains("blade/socket_blade")) return "Sword Blade";
        if (path.contains("blade/")) return title(last(path)) + (path.contains("blade/mace") ? " Head" : " Blade");
        if (path.contains("tool/pickaxe")) return "Pickaxe Head";
        if (path.contains("tool/axe")) return "Axe Head";
        if (path.contains("tool/shovel")) return "Shovel Head";
        if (path.contains("tool/hoe")) return "Hoe Head";
        if (path.contains("tool/hammer")) return "Hammer Head";
        if (path.contains("guard/")) return title(last(path)) + " Guard";
        if (path.contains("pommel/")) return title(last(path)) + " Pommel";
        return title(last(path));
    }

    static String equipmentName(List<String> moduleIds) {
        String joined = String.join(" ", moduleIds).toLowerCase(Locale.ROOT);
        if (joined.contains("blade/greatsword")) return "Greatsword";
        if (joined.contains("blade/longsword")) return "Longsword";
        if (joined.contains("blade/katana")) return "Katana";
        if (joined.contains("blade/rapier")) return "Rapier";
        if (joined.contains("blade/dagger")) return "Dagger";
        if (joined.contains("blade/scythe")) return "Scythe";
        if (joined.contains("blade/sickle")) return "Sickle";
        if (joined.contains("blade/spear")) return "Spear";
        if (joined.contains("blade/trident")) return "Trident";
        if (joined.contains("blade/mace")) return "Mace";
        if (joined.contains("blade/zweihaender")) return "Zweihänder";
        if (joined.contains("blade/throwing_knife")) return "Throwing Knife";
        if (joined.contains("blade/")) return "Sword";
        if (joined.contains("tool/pickaxe")) return "Pickaxe";
        if (joined.contains("tool/axe")) return "Axe";
        if (joined.contains("tool/shovel")) return "Shovel";
        if (joined.contains("tool/hoe")) return "Hoe";
        if (joined.contains("tool/hammer")) return "Hammer";
        if (joined.contains("shield/")) return "Shield";
        if (joined.contains("handle/sword")) return "Sword";
        if (joined.contains("handle/tool")) return "Tool";
        if (joined.contains("handle/polearm")) return "Polearm";
        return partName(moduleIds.getFirst());
    }

    static String materialName(String materialId) {
        return title(last(path(materialId)));
    }

    private static String path(String moduleId) {
        int separator = moduleId.indexOf(':');
        return separator >= 0 ? moduleId.substring(separator + 1) : moduleId;
    }

    private static String last(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static String title(String value) {
        String[] words = value.replace('-', '_').split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
