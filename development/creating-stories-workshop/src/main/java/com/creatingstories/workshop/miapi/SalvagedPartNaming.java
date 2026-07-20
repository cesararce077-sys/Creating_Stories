package com.creatingstories.workshop.miapi;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ModuleInstance;

import java.util.ArrayList;
import java.util.List;

public final class SalvagedPartNaming {
    private SalvagedPartNaming() {}

    public static Component name(ModuleInstance removed, Material material) {
        List<String> moduleIds = new ArrayList<>();
        collectModuleIds(removed, moduleIds);
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

    private static void collectModuleIds(ModuleInstance module, List<String> output) {
        output.add(module.moduleId().toString());
        module.children().values().forEach(child -> collectModuleIds(child, output));
    }

}
