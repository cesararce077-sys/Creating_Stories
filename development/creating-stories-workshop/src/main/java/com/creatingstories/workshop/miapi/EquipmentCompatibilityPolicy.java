package com.creatingstories.workshop.miapi;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Pack policy for automatic ranged-weapon and armor conversion.
 *
 * <p>Automatic conversion is intentionally conservative: stat-only material
 * variants enter the Workshop, while equipment whose identity may live in its
 * item class, components, or scripted behavior remains an authored sidegrade.</p>
 */
public final class EquipmentCompatibilityPolicy {
    public enum Kind { RANGED, ARMOR }
    public enum Classification { ORDINARY, SPECIAL, EXCLUDED }

    private static final Set<String> VANILLA_RANGED = Set.of("bow", "crossbow", "arrow");
    private static final Pattern VANILLA_ARMOR = Pattern.compile(
        "(?:leather|chainmail|iron|golden|diamond|netherite)_(?:helmet|chestplate|leggings|boots)");
    private static final Pattern EXPANDED_COMBAT_RANGED = Pattern.compile(
        "(?:iron|golden|diamond|netherite)_(?:bow|cross_bow|arrow|cross_bow_arrow)");
    private static final Pattern DEEPER_DARKER_ARMOR = Pattern.compile(
        "(?:resonarium|warden)_(?:helmet|chestplate|leggings|boots)");
    private static final Pattern ICE_AND_FIRE_MUNDANE_ARMOR = Pattern.compile(
        "armor_(?:copper|silver)_metal_(?:helmet|chestplate|leggings|boots)");
    private static final Pattern CREATE_SA_ARMOR = Pattern.compile(
        "(?:copper|zinc|brass)_(?:helmet|chestplate|leggings|boots)");

    private EquipmentCompatibilityPolicy() {}

    public static Classification classify(ResourceLocation id, Kind kind) {
        return classify(id.getNamespace(), id.getPath(), kind);
    }

    public static Classification classify(String namespace, String path, Kind kind) {
        if (isOrdinary(namespace, path, kind)) return Classification.ORDINARY;
        if (namespace.equals("cataclysm") || namespace.equals("mowziesmobs")
            || namespace.equals("alexscaves") || namespace.equals("irons_spellbooks")) {
            return Classification.EXCLUDED;
        }
        return Classification.SPECIAL;
    }

    public static boolean isOrdinary(String namespace, String path, Kind kind) {
        if (namespace.equals("minecraft")) {
            return kind == Kind.RANGED
                ? VANILLA_RANGED.contains(path)
                : VANILLA_ARMOR.matcher(path).matches() || path.equals("elytra");
        }
        return switch (kind) {
            case RANGED -> namespace.equals("expanded_combat")
                && EXPANDED_COMBAT_RANGED.matcher(path).matches();
            case ARMOR -> switch (namespace) {
                case "deeperdarker" -> DEEPER_DARKER_ARMOR.matcher(path).matches();
                case "iceandfire" -> ICE_AND_FIRE_MUNDANE_ARMOR.matcher(path).matches();
                case "create_sa" -> CREATE_SA_ARMOR.matcher(path).matches();
                default -> false;
            };
        };
    }
}
