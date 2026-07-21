package com.creatingstories.workshop.miapi;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ModuleInstance;

import java.util.ArrayList;
import java.util.List;

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

        String equipmentName = SalvagedPartNameRules.completeEquipmentName(moduleIds);
        if (equipmentName != null) {
            Material equipmentMaterial = functionalMaterial(modules);
            if (equipmentMaterial == null) equipmentMaterial = material;
            Component materialComponent = equipmentMaterial == null
                ? Component.literal("Modular")
                : materialName(equipmentMaterial);
            return Component.translatable("creating_stories_workshop.complete_equipment_name",
                materialComponent, Component.literal(equipmentName));
        }

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

    private static Component materialName(Material material) {
        Component translated = material.getTranslation();
        if (translated.getContents() instanceof TranslatableContents contents
            && translated.getString().equals(contents.getKey())) {
            return Component.literal(SalvagedPartNameRules.materialName(material.getID().toString()));
        }
        return translated;
    }

    private static Material functionalMaterial(List<ModuleInstance> modules) {
        for (ModuleInstance module : modules) {
            if (!SalvagedPartNameRules.isFunctionalPart(module.moduleId().toString())) continue;
            Material material = MaterialProperty.getMaterial(module);
            if (material != null) return material;
        }
        return null;
    }

    private static void collectModules(ModuleInstance module, List<ModuleInstance> output) {
        output.add(module);
        module.children().values().forEach(child -> collectModules(child, output));
    }

}
