package com.creatingstories.workshop.fusion;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.MutableModuleInstance;
import smartin.miapi.modules.properties.slot.SlotProperty;

import java.util.Arrays;
import java.util.List;

public final class ModuleFusion {
    public static final int SINGLE_COMPONENT_LEVEL_COST = 5;

    private ModuleFusion() {}

    public static FusionResult preview(ItemStack base, ItemStack donor, String rawPath) {
        return preview(base, donor, rawPath, rawPath);
    }

    public static FusionResult previewRootDonor(ItemStack base, ItemStack donor, String destinationPath) {
        return preview(base, donor, destinationPath, "");
    }

    private static FusionResult preview(ItemStack base, ItemStack donor, String destinationPath, String donorPath) {
        if (base.isEmpty() || donor.isEmpty()) {
            return FusionResult.failure(Component.translatable("commands.creating_stories_workshop.fusion.need_items"));
        }

        ModuleInstance baseRoot = ItemModule.getModules(base);
        ModuleInstance donorRoot = ItemModule.getModules(donor);
        if (baseRoot.getModule().isEmpty() || donorRoot.getModule().isEmpty()) {
            return FusionResult.failure(Component.translatable("commands.creating_stories_workshop.fusion.need_items"));
        }

        List<String> path = parsePath(destinationPath);
        if (path.isEmpty()) {
            return FusionResult.failure(Component.translatable("commands.creating_stories_workshop.fusion.root_forbidden"));
        }

        List<String> sourcePath = parsePath(donorPath);
        ModuleInstance donorPart = donorRoot.getPosition(sourcePath);
        if (donorPart == null) {
            return FusionResult.failure(Component.translatable("commands.creating_stories_workshop.fusion.bad_path", donorPath));
        }

        List<String> parentPath = path.subList(0, path.size() - 1);
        String destinationId = path.getLast();
        ModuleInstance baseParent = baseRoot.getPosition(parentPath);
        if (baseParent == null) {
            return FusionResult.failure(Component.translatable("commands.creating_stories_workshop.fusion.missing_destination", destinationId));
        }

        SlotProperty.ModuleSlot destination = SlotProperty.getSlots(baseParent).get(destinationId);
        if (destination == null) {
            return FusionResult.failure(Component.translatable("commands.creating_stories_workshop.fusion.missing_destination", destinationId));
        }
        if (!destination.allowedIn(donorPart)) {
            return FusionResult.failure(Component.translatable(
                "commands.creating_stories_workshop.fusion.incompatible", donorPart.moduleId(), destinationId));
        }

        ModuleInstance currentPart = baseRoot.getPosition(path);
        if (donorPart.equals(currentPart)) {
            return FusionResult.failure(Component.translatable("commands.creating_stories_workshop.fusion.same"));
        }

        ItemStack output = base.copy();
        MutableModuleInstance mutableRoot = baseRoot.asMutable();
        MutableModuleInstance mutableParent = mutableRoot.getPosition(parentPath);
        mutableParent.setChild(destinationId, donorPart.asMutable());
        mutableRoot.toRecord().writeToItem(output);

        return new FusionResult(true, output, SINGLE_COMPONENT_LEVEL_COST,
            Component.translatable("commands.creating_stories_workshop.fusion.preview",
                destinationPath, donorPart.moduleId(), SINGLE_COMPONENT_LEVEL_COST));
    }

    static List<String> parsePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank() || "/".equals(rawPath.trim())) {
            return List.of();
        }
        return Arrays.stream(rawPath.split("/"))
            .map(String::trim)
            .filter(segment -> !segment.isEmpty())
            .toList();
    }
}
