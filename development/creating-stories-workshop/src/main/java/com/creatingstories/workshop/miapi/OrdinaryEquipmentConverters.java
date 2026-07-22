package com.creatingstories.workshop.miapi;

import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.ItemToModularConverter;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.registries.RegistryInventory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Pack-owned converters for ordinary equipment whose materials are predefined. */
public final class OrdinaryEquipmentConverters {
    private static final ResourceLocation MATERIAL = ResourceLocation.fromNamespaceAndPath("miapi", "material");
    private static final String WOOD = "miapi:wood/wood";
    private static final String STRING = "miapi:rope/string";
    private static final String FEATHER = "miapi:fletching/feather";
    private static final String IRON = "miapi:metal/iron";
    private static final Map<String, ItemStack> PACK_CONVERTERS = new ConcurrentHashMap<>();
    private static boolean registered;

    private OrdinaryEquipmentConverters() {}

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        ReloadEvents.END.subscribe((isClient, registryAccess, worker) -> {
            addExpandedCombatRanged();
            addArmorFamily("create_sa", "copper", "miapi:metal/copper");
            addArmorFamily("create_sa", "zinc", "creating_stories_workshop:metal/zinc");
            addArmorFamily("create_sa", "brass", "creating_stories_workshop:metal/brass");
            addArmorFamily("iceandfire", "armor_copper_metal", "miapi:metal/copper");
            ItemToModularConverter.regexes.putAll(PACK_CONVERTERS);
            Miapi.LOGGER.info("Creating Stories restored {} ranged/armor converters after {} reload",
                PACK_CONVERTERS.size(), isClient ? "client" : "server");
        });
    }

    private static void addExpandedCombatRanged() {
        Map<String, String> materials = new LinkedHashMap<>();
        materials.put("iron", IRON);
        materials.put("golden", "miapi:metal/gold");
        materials.put("diamond", "miapi:crystal/diamond");
        materials.put("netherite", "miapi:metal/netherite");
        materials.forEach((name, material) -> {
            add("expanded_combat", name + "_bow", bowTree(material));
            add("expanded_combat", name + "_cross_bow", crossbowTree(material));
            add("expanded_combat", name + "_arrow", arrowTree(material));
            add("expanded_combat", name + "_cross_bow_arrow", arrowTree(material));
        });
    }

    private static void addArmorFamily(String namespace, String prefix, String material) {
        for (String slot : List.of("helmet", "chestplate", "leggings", "boots")) {
            add(namespace, prefix + "_" + slot, armorTree(slot, material));
        }
    }

    private static void add(String namespace, String path, ModuleInstance root) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        if (!BuiltInRegistries.ITEM.containsKey(id)) return;
        ItemStack output = new ItemStack(RegistryInventory.modularItem);
        root.writeToItem(output);
        String regex = Pattern.quote(id.toString());
        PACK_CONVERTERS.put(regex, output);
        ItemToModularConverter.regexes.put(regex, output);
        Miapi.LOGGER.info("Creating Stories added ordinary equipment converter for {}", id);
    }

    private static ModuleInstance bowTree(String material) {
        return module("tm_archery:bow/handle/normal", material, Map.of(
            "arms", module("tm_archery:bow/arm/normal", material, Map.of(
                "string", module("tm_archery:bow/string/normal", STRING, Map.of())))));
    }

    private static ModuleInstance crossbowTree(String material) {
        return module("tm_archery:crossbow/stock/normal", material, Map.of(
            "arms", module("tm_archery:crossbow/arms/normal", material, Map.of(
                "string", module("tm_archery:crossbow/string/normal", STRING, Map.of()))),
            "attachments", module("tm_archery:crossbow/attachments/styrup", IRON, Map.of())));
    }

    private static ModuleInstance arrowTree(String material) {
        return module("tm_archery:arrow/shaft/normal", WOOD, Map.of(
            "head", module("tm_archery:arrow/head/normal", material, Map.of()),
            "shaft", module("tm_archery:arrow/tail/fletching", FEATHER, Map.of())));
    }

    private static ModuleInstance armorTree(String slot, String material) {
        return switch (slot) {
            case "helmet" -> module("tm_armory:armor/helmet", null, Map.of(
                "hat", module("tm_armory:armor/default/helmet", material, Map.of())));
            case "chestplate" -> module("tm_armory:armor/chestplate", null, Map.of(
                "chest_front", module("tm_armory:armor/default/front_chest", material, Map.of()),
                "chest_back", module("tm_armory:armor/default/back_chest", material, Map.of()),
                "arm_left", module("tm_armory:armor/default/arm_left", material, Map.of()),
                "arm_right", module("tm_armory:armor/default/arm_right", material, Map.of())));
            case "leggings" -> module("tm_armory:armor/pants", null, Map.of(
                "belt", module("tm_armory:armor/default/belt", material, Map.of()),
                "leg_left", module("tm_armory:armor/default/leg_left", material, Map.of()),
                "leg_right", module("tm_armory:armor/default/leg_right", material, Map.of())));
            case "boots" -> module("tm_armory:armor/boots", null, Map.of(
                "boot_left", module("tm_armory:armor/default/boot_left", material, Map.of()),
                "boot_right", module("tm_armory:armor/default/boot_right", material, Map.of())));
            default -> throw new IllegalArgumentException("Unknown armor slot: " + slot);
        };
    }

    private static ModuleInstance module(String id, String material, Map<String, ModuleInstance> children) {
        Map<ResourceLocation, com.google.gson.JsonElement> data = material == null
            ? Map.of()
            : Map.of(MATERIAL, new JsonPrimitive(material));
        return new ModuleInstance(ResourceLocation.parse(id), data, children, Miapi.registryAccess);
    }
}
