package com.creatingstories.workshop.miapi;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.MutableModuleInstance;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.modules.properties.render.ServerReplaceProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

public final class PartInstallCrafting extends ServerReplaceProperty implements CraftingProperty {
    public static final ResourceLocation KEY = ResourceLocation.fromNamespaceAndPath(
        "creating_stories_workshop", "part_source_slot");
    public static final int LEVEL_COST = 5;

    @Override
    public boolean shouldExecuteOnCraft(@Nullable ModuleInstance module, ModuleInstance root,
                                        ItemStack stack, CraftAction action) {
        return action.data.containsKey(KEY);
    }

    @Override
    public float getPriority() {
        return -20;
    }

    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, @Nullable ModularWorkBenchEntity bench,
                              @Nullable Player player, CraftAction action, ItemModule module,
                              List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data,
                              Consumer<Component> failreason) {
        ItemStack source = sourceStack(action, data);
        ModuleInstance donor = ItemModule.getModules(source);
        if (source.isEmpty() || donor.getModule().isEmpty() || !isCompatible(old, action.slotLocation, donor)) {
            failreason.accept(Component.translatable("creating_stories_workshop.workbench.part_invalid"));
            return false;
        }
        if (action.toAdd == null || !action.toAdd.id().equals(donor.moduleId())) {
            failreason.accept(Component.translatable("creating_stories_workshop.workbench.part_changed"));
            return false;
        }
        ModuleInstance existing = ItemModule.getModules(old).getPosition(action.slotLocation);
        if (existing != null && wouldDiscardChildren(existing, donor)) {
            failreason.accept(Component.translatable(
                "creating_stories_workshop.workbench.incompatible_children"));
            return false;
        }
        if (player != null && !player.isCreative() && player.experienceLevel < LEVEL_COST) {
            failreason.accept(Component.translatable(
                "creating_stories_workshop.workbench.levels_required", LEVEL_COST));
            return false;
        }
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player,
                             ModularWorkBenchEntity bench, CraftAction action, ItemModule module,
                             List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data) {
        ItemStack source = sourceStack(action, data);
        ModuleInstance donor = ItemModule.getModules(source);
        if (source.isEmpty() || donor.getModule().isEmpty()) return crafting;

        ModuleInstance craftedRoot = ItemModule.getModules(crafting);
        ModuleInstance oldTarget = ItemModule.getModules(old).getPosition(action.slotLocation);
        MutableModuleInstance replacement = mergePreservedChildren(oldTarget, donor);
        MutableModuleInstance root = craftedRoot.asMutable();
        List<String> path = action.slotLocation;
        if (path.isEmpty()) {
            replacement.toRecord().writeToItem(crafting);
            return crafting;
        }
        MutableModuleInstance parent = root.getPosition(path.subList(0, path.size() - 1));
        if (parent != null) {
            parent.setChild(path.getLast(), replacement);
            root.toRecord().writeToItem(crafting);
        }
        return crafting;
    }

    @Override
    public List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, Player player,
                                              @Nullable ModularWorkBenchEntity bench, CraftAction action,
                                              ItemModule module, List<ItemStack> inventory,
                                              Map<ResourceLocation, JsonElement> data) {
        ItemStack source = sourceStack(action, data);
        ItemStack result = preview(old, crafting, player, bench, action, module, inventory, data);
        if (player != null && !player.level().isClientSide()) {
            if (!player.isCreative()) player.giveExperienceLevels(-LEVEL_COST);
            source.shrink(1);
            action.screenHandler.broadcastChanges();
        }
        // MIAPI mutates this return value with removeFirst() while committing the
        // crafting-property pipeline, so it must remain mutable.
        List<ItemStack> results = new ArrayList<>();
        results.add(result);
        return results;
    }

    public static boolean isCompatible(ItemStack target, List<String> path, ModuleInstance donor) {
        if (path.isEmpty()) return true;
        ModuleInstance root = ItemModule.getModules(target);
        ModuleInstance parent = root.getPosition(path.subList(0, path.size() - 1));
        if (parent == null) return false;
        SlotProperty.ModuleSlot slot = SlotProperty.getSlots(parent).get(path.getLast());
        return slot != null && slot.allowedIn(donor);
    }

    /**
     * Builds the installed subtree. Children explicitly carried by the donor
     * replace the corresponding target children. Compatible target children
     * missing from the donor survive, preventing a bare handle or guard from
     * silently deleting the valuable assembly attached to it.
     */
    private static MutableModuleInstance mergePreservedChildren(@Nullable ModuleInstance existing,
                                                                 ModuleInstance donor) {
        MutableModuleInstance merged = donor.asMutable();
        if (existing == null) return merged;

        Map<String, SlotProperty.ModuleSlot> donorSlots = SlotProperty.getSlots(donor);
        existing.children().forEach((slotId, existingChild) -> {
            if (donor.children().containsKey(slotId)) return;
            SlotProperty.ModuleSlot donorSlot = donorSlots.get(slotId);
            if (donorSlot != null && donorSlot.allowedIn(existingChild)) {
                merged.setChild(slotId, existingChild.asMutable());
            }
        });
        return merged;
    }

    private static boolean wouldDiscardChildren(ModuleInstance existing, ModuleInstance donor) {
        Map<String, SlotProperty.ModuleSlot> donorSlots = SlotProperty.getSlots(donor);
        for (Map.Entry<String, ModuleInstance> entry : existing.children().entrySet()) {
            if (donor.children().containsKey(entry.getKey())) continue;
            SlotProperty.ModuleSlot donorSlot = donorSlots.get(entry.getKey());
            if (donorSlot == null || !donorSlot.allowedIn(entry.getValue())) return true;
        }
        return false;
    }

    private static ItemStack sourceStack(CraftAction action, Map<ResourceLocation, JsonElement> data) {
        JsonElement element = data.get(KEY);
        if (element == null || !element.isJsonPrimitive()) return ItemStack.EMPTY;
        int index = element.getAsInt();
        if (index < 0 || index >= action.screenHandler.slots.size()) return ItemStack.EMPTY;
        return action.screenHandler.slots.get(index).getItem();
    }
}
