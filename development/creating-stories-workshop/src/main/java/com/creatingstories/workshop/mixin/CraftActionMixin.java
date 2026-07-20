package com.creatingstories.workshop.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.registries.RegistryInventory;
import com.creatingstories.workshop.miapi.SalvagedPartNaming;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

@Mixin(value = CraftAction.class, remap = false)
public abstract class CraftActionMixin {
    @Shadow private ItemStack old;
    @Shadow @Final public ItemModule toAdd;
    @Shadow @Final public Player player;
    @Shadow @Final public List<String> slotLocation;

    @Inject(method = "perform", at = @At("RETURN"))
    private void creatingstories$returnRemovedPart(CallbackInfoReturnable<ItemStack> cir) {
        if (player == null || player.level().isClientSide() || toAdd == null) return;

        ModuleInstance previous = ItemModule.getModules(old).getPosition(slotLocation);
        if (previous == null || previous.getModule().isEmpty()) return;

        ItemStack crafted = cir.getReturnValue();
        ModuleInstance current = crafted.isEmpty()
            ? null
            : ItemModule.getModules(crafted).getPosition(slotLocation);
        if (current != null && current.getModule().isEmpty()) current = null;

        ModuleInstance removed = displacedTree(previous, current);
        if (removed == null) return;

        ItemStack detached = new ItemStack(RegistryInventory.visualOnlymodularItem);
        removed.copy().writeToItem(detached);
        ComponentApplyProperty.updateItemStack(detached, player.level().registryAccess());

        // A module's item_id describes the complete equipment it normally builds
        // (for example, a sword blade declares modular_sword). Do not apply it to
        // salvage: the visual-only MIAPI part is the correct detached container.
        Material material = MaterialProperty.getMaterial(removed);
        Component detachedName = SalvagedPartNaming.name(removed, material);
        detached.set(DataComponents.CUSTOM_NAME,
            detachedName.copy().withStyle(style -> style.withItalic(false)));

        if (!player.getInventory().add(detached)) {
            player.drop(detached, false);
        }
    }

    /**
     * Returns a standalone tree containing only state displaced by the craft.
     * Children preserved by the hierarchy merge are omitted so returning a
     * parent component cannot duplicate its still-installed descendants.
     */
    private static @Nullable ModuleInstance displacedTree(ModuleInstance previous,
                                                           @Nullable ModuleInstance current) {
        if (current == null) return previous.copy();

        boolean localChanged = !previous.moduleId().equals(current.moduleId())
            || !previous.data().equals(current.data());
        Map<String, ModuleInstance> displacedChildren = new LinkedHashMap<>();

        previous.children().forEach((slot, previousChild) -> {
            ModuleInstance currentChild = current.children().get(slot);
            ModuleInstance displacedChild = displacedTree(previousChild, currentChild);
            if (displacedChild != null) displacedChildren.put(slot, displacedChild);
        });

        if (!localChanged && displacedChildren.isEmpty()) return null;
        return new ModuleInstance(
            previous.moduleId(),
            previous.data(),
            displacedChildren,
            previous.getter());
    }
}
