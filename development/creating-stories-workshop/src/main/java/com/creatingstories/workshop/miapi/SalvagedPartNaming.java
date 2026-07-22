package com.creatingstories.workshop.miapi;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ModuleInstance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SalvagedPartNaming {
    private SalvagedPartNaming() {}

    public static boolean isWorkshopManagedName(Component name) {
        if (name == null) return false;
        String rendered = name.getString();
        if (rendered.startsWith("miapi.") || rendered.contains(".miapi.")) return true;
        return name.getContents() instanceof TranslatableContents contents
            && SalvagedPartNameRules.isWorkshopManagedTranslationKey(contents.getKey());
    }

    public static Component name(ModuleInstance removed, Material material) {
        List<ModuleInstance> modules = new ArrayList<>();
        collectModules(removed, modules);
        List<String> moduleIds = new ArrayList<>();
        modules.forEach(module -> moduleIds.add(module.moduleId().toString()));

        Component completeName = completeEquipmentName(modules, moduleIds, material);
        if (completeName != null) return completeName;

        if (moduleIds.size() > 1) {
            return Component.translatable("creating_stories_workshop.salvaged_assembly_name",
                Component.literal(SalvagedPartNameRules.equipmentName(moduleIds)));
        }

        Component role = Component.literal(SalvagedPartNameRules.partName(removed.moduleId().toString()));
        if (material == null) {
            return Component.translatable("creating_stories_workshop.salvaged_part_without_material", role);
        }
        return Component.translatable("creating_stories_workshop.salvaged_part_name",
            materialName(material), role);
    }

    /** Returns a stable semantic name only when the module tree is complete equipment. */
    public static Component completeEquipmentName(ModuleInstance root, Material fallbackMaterial) {
        List<ModuleInstance> modules = new ArrayList<>();
        collectModules(root, modules);
        List<String> moduleIds = new ArrayList<>();
        modules.forEach(module -> moduleIds.add(module.moduleId().toString()));
        return completeEquipmentName(modules, moduleIds, fallbackMaterial);
    }

    private static Component completeEquipmentName(List<ModuleInstance> modules, List<String> moduleIds,
                                                    Material fallbackMaterial) {
        String equipmentName = SalvagedPartNameRules.completeEquipmentName(moduleIds);
        if (equipmentName == null) return null;
        Material equipmentMaterial = functionalMaterial(modules);
        if (equipmentMaterial == null) equipmentMaterial = fallbackMaterial;
        Component materialComponent = equipmentMaterial == null
            ? Component.literal("Modular")
            : materialName(equipmentMaterial);
        return Component.translatable("creating_stories_workshop.complete_equipment_name",
            materialComponent, Component.literal(equipmentName));
    }

    private static Component materialName(Material material) {
        Component translated = material.getTranslation();
        if (translated.getContents() instanceof TranslatableContents contents
            && translated.getString().equals(contents.getKey())) {
            return Component.literal(SalvagedPartNameRules.materialName(material.getID().toString()));
        }
        return translated;
    }

    private static Material functionalMaterial(List<ModuleInstance> modules) {
        Map<String, Material> materials = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ModuleInstance module : modules) {
            if (!SalvagedPartNameRules.isFunctionalPart(module.moduleId().toString())) continue;
            Material material = MaterialProperty.getMaterial(module);
            if (material == null) continue;
            String id = material.getID().toString();
            materials.putIfAbsent(id, material);
            counts.merge(id, 1, Integer::sum);
        }
        String dominant = null;
        int highest = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > highest) {
                dominant = entry.getKey();
                highest = entry.getValue();
            }
        }
        return dominant == null ? null : materials.get(dominant);
    }

    private static void collectModules(ModuleInstance module, List<ModuleInstance> output) {
        output.add(module);
        module.children().values().forEach(child -> collectModules(child, output));
    }

}
