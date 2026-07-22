package com.creatingstories.workshop.miapi;

import java.util.List;
import java.util.Locale;

final class SalvagedPartNameRules {
    private SalvagedPartNameRules() {}

    static boolean isWorkshopManagedTranslationKey(String key) {
        return key != null && (key.startsWith("creating_stories_workshop.salvaged_")
            || key.equals("creating_stories_workshop.complete_equipment_name"));
    }

    static String partName(String moduleId) {
        String path = path(moduleId);
        if (path.equals("armor/helmet")) return "Helmet Liner";
        if (path.equals("armor/chestplate")) return "Chestplate Liner";
        if (path.equals("armor/pants")) return "Leggings Liner";
        if (path.equals("armor/boots")) return "Boot Liners";
        if (path.contains("crossbow/stock/")) return variantRole(path, "Crossbow Stock");
        if (path.contains("crossbow/arms/")) return variantRole(path, "Crossbow Limbs");
        if (path.contains("crossbow/string/")) return stringName(path, "Crossbow String");
        if (path.contains("crossbow/attachments/")) {
            String attachment = last(path).equals("styrup") ? "Stirrup" : title(last(path));
            return "Crossbow " + attachment;
        }
        if (path.contains("bow/handle/")) return variantRole(path, "Bow Grip");
        if (path.contains("bow/arm/")) return variantRole(path, "Bow Limbs");
        if (path.contains("bow/string/")) return stringName(path, "Bowstring");
        if (path.contains("arrow/shaft/")) return variantRole(path, "Arrow Shaft");
        if (path.contains("arrow/head/")) return variantRole(path, "Arrowhead");
        if (path.contains("arrow/tail/")) return title(last(path));
        if (path.contains("armor/")) return armorPartName(path);
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
        if (joined.contains("crossbow/")) return "Crossbow";
        if (joined.contains("bow/")) return "Bow";
        if (joined.contains("arrow/")) return "Arrow";
        if (joined.contains("armor/wing/") || joined.contains("elytra_backplate")) return "Elytra";
        if (joined.contains("armor/chestplate") || joined.contains("front_chest")
            || joined.contains("back_chest")) return "Chestplate";
        if (joined.contains("armor/helmet") || joined.matches(".*armor/[^ ]+/helmet.*")) return "Helmet";
        if (joined.contains("armor/pants") || joined.contains("leg_left")
            || joined.contains("leg_right") || joined.contains("armor/default/belt")) return "Leggings";
        if (joined.contains("armor/boots") || joined.contains("boot_left")
            || joined.contains("boot_right")) return "Boots";
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
        if (joined.contains("blade/zweihaender")) return "Zweihander";
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

    /** Identifies visual-only trees that already form complete functional equipment. */
    static String completeEquipmentName(List<String> moduleIds) {
        if (moduleIds.isEmpty()) return null;
        String root = path(moduleIds.getFirst()).toLowerCase(Locale.ROOT);
        List<String> paths = moduleIds.stream()
            .map(SalvagedPartNameRules::path)
            .map(value -> value.toLowerCase(Locale.ROOT))
            .toList();
        boolean hasBlade = paths.stream().anyMatch(value -> value.contains("blade/"));
        boolean hasToolHead = paths.stream().anyMatch(value -> value.contains("tool/"));

        if (root.contains("handle/sword") && hasBlade) return equipmentName(moduleIds);
        if (root.contains("handle/tool") && hasToolHead) return equipmentName(moduleIds);
        if (root.contains("handle/polearm") && hasBlade) return equipmentName(moduleIds);
        if (root.contains("bow/handle/")
            && paths.stream().anyMatch(value -> value.contains("bow/arm/"))
            && paths.stream().anyMatch(value -> value.contains("bow/string/"))) return "Bow";
        if (root.contains("crossbow/stock/")
            && paths.stream().anyMatch(value -> value.contains("crossbow/arms/"))
            && paths.stream().anyMatch(value -> value.contains("crossbow/string/"))) return "Crossbow";
        if (root.contains("arrow/shaft/")
            && paths.stream().anyMatch(value -> value.contains("arrow/head/"))
            && paths.stream().anyMatch(value -> value.contains("arrow/tail/"))) return "Arrow";
        if (root.equals("armor/helmet")
            && paths.stream().anyMatch(SalvagedPartNameRules::isHelmetPart)) return "Helmet";
        if (root.equals("armor/chestplate")
            && paths.stream().anyMatch(value -> value.contains("front_chest")
                || value.contains("elytra_backplate"))) return equipmentName(moduleIds);
        if (root.equals("armor/pants")
            && paths.stream().anyMatch(value -> value.contains("leg_left") || value.contains("leg_right"))) {
            return "Leggings";
        }
        if (root.equals("armor/boots")
            && paths.stream().anyMatch(value -> value.contains("boot_left") || value.contains("boot_right"))) {
            return "Boots";
        }
        return null;
    }

    static boolean isFunctionalPart(String moduleId) {
        String path = path(moduleId).toLowerCase(Locale.ROOT);
        return path.contains("blade/") || path.contains("tool/") || path.contains("shield/")
            || path.contains("bow/arm/") || path.contains("crossbow/arms/")
            || path.contains("arrow/head/") || isArmorPlate(path);
    }

    static String materialName(String materialId) {
        String materialPath = path(materialId);
        if (materialPath.startsWith("generated/")) {
            String generatedName = generatedMaterialName(materialPath);
            if (!generatedName.isEmpty()) return title(generatedName);
        }
        return title(last(materialPath));
    }

    private static String path(String moduleId) {
        int separator = moduleId.indexOf(':');
        return separator >= 0 ? moduleId.substring(separator + 1) : moduleId;
    }

    private static String last(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static String stringName(String path, String normalName) {
        String variant = last(path);
        return variant.equals("normal") ? normalName : title(variant) + " " + normalName;
    }

    private static String variantRole(String path, String role) {
        String variant = last(path);
        return variant.equals("normal") ? role : title(variant) + " " + role;
    }

    private static String armorPartName(String path) {
        String leaf = last(path);
        if (path.contains("armor/wing/")) {
            return switch (leaf) {
                case "left" -> "Left Wing";
                case "right" -> "Right Wing";
                case "elytra_backplate" -> "Elytra Backplate";
                default -> title(leaf);
            };
        }
        String variant = armorVariant(path);
        String role = switch (leaf) {
            case "front_chest" -> "Front Chestplate";
            case "back_chest" -> "Back Chestplate";
            case "arm_left" -> "Left Arm Guard";
            case "arm_right" -> "Right Arm Guard";
            case "leg_left" -> "Left Leg Guard";
            case "leg_right" -> "Right Leg Guard";
            case "boot_left" -> "Left Boot";
            case "boot_right" -> "Right Boot";
            case "helmet" -> "Helmet Shell";
            case "belt" -> "Armor Belt";
            default -> title(leaf);
        };
        return variant.isEmpty() || variant.equals("Default") ? role : variant + " " + role;
    }

    private static String armorVariant(String path) {
        String[] segments = path.split("/");
        if (segments.length < 3 || !segments[0].equals("armor")) return "";
        return title(segments[1]);
    }

    private static String generatedMaterialName(String path) {
        int itemKey = path.indexOf("item.");
        if (itemKey < 0) return "";
        int namespaceEnd = path.indexOf('.', itemKey + "item.".length());
        if (namespaceEnd < 0 || namespaceEnd + 1 >= path.length()) return "";
        int valueStart = namespaceEnd + 1;
        int nextItem = path.indexOf("item.", valueStart);
        String value = nextItem < 0 ? path.substring(valueStart) : path.substring(valueStart, nextItem);
        for (String suffix : List.of("_ingot", "_gem", "_crystal", "_shard", "_chunk", "_nugget", "_block")) {
            if (value.endsWith(suffix) && value.length() > suffix.length()) {
                return value.substring(0, value.length() - suffix.length());
            }
        }
        return value;
    }

    private static boolean isHelmetPart(String path) {
        return !path.equals("armor/helmet") && (path.endsWith("/helmet")
            || path.contains("armor/hat/") || path.contains("armor/head/"));
    }

    private static boolean isArmorPlate(String path) {
        if (!path.contains("armor/") || path.equals("armor/helmet")
            || path.equals("armor/chestplate") || path.equals("armor/pants")
            || path.equals("armor/boots")) return false;
        return path.contains("front_chest") || path.contains("back_chest")
            || path.contains("arm_left") || path.contains("arm_right")
            || path.contains("leg_left") || path.contains("leg_right")
            || path.contains("boot_left") || path.contains("boot_right")
            || path.endsWith("/helmet") || path.contains("elytra_backplate");
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
