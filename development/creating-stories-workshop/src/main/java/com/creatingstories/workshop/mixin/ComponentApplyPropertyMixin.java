package com.creatingstories.workshop.mixin;

import com.creatingstories.workshop.miapi.SalvagedPartNaming;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.registries.RegistryInventory;
import smartin.miapi.item.modular.ModularItem;

@Mixin(value = ComponentApplyProperty.class, remap = false)
public interface ComponentApplyPropertyMixin {
    @Inject(method = "updateItemStack", at = @At("RETURN"))
    private static void creatingstories$repairStandalonePartName(ItemStack stack,
                                                                  RegistryAccess registryAccess,
                                                                  CallbackInfo ci) {
        boolean visualOnly = stack.is(RegistryInventory.visualOnlymodularItem);
        boolean functionalModular = ModularItem.isModularItem(stack);
        Component currentName = stack.get(DataComponents.CUSTOM_NAME);
        if (currentName == null && !visualOnly && !functionalModular) return;
        if (currentName != null && !SalvagedPartNaming.isWorkshopManagedName(currentName)) return;

        ModuleInstance module = ItemModule.getModules(stack);
        if (module.getModule().isEmpty()) return;
        Component repaired = functionalModular && !visualOnly
            ? SalvagedPartNaming.completeEquipmentName(module, MaterialProperty.getMaterial(module))
            : SalvagedPartNaming.name(module, MaterialProperty.getMaterial(module));
        if (repaired == null) return;
        stack.set(DataComponents.CUSTOM_NAME,
            repaired.copy().withStyle(style -> style.withItalic(false)));
    }
}
