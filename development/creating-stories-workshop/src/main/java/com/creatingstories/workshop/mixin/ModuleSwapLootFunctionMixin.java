package com.creatingstories.workshop.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.loot.ModuleSwapLootFunction;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.MutableModuleInstance;

import java.util.ArrayList;
import java.util.Map;

/**
 * MIAPI 2.3.8 only rolls the root module despite this method's name. Reuse its
 * own substitution logic for each installed descendant. This retains slot,
 * material, blacklist, whitelist and allowed-in-loot checks while allowing
 * blades, guards, bindings, pommels and tool heads to vary independently.
 */
@Mixin(value = ModuleSwapLootFunction.class, remap = false)
public abstract class ModuleSwapLootFunctionMixin {
    @Shadow
    abstract MutableModuleInstance randomizeModuleAndChildren(
        MutableModuleInstance module, Material baseline, RandomSource random);

    /** See {@link MaterialSwapLootFunctionMixin} for the matching chain fix. */
    @Redirect(
        method = "apply",
        at = @At(
            value = "INVOKE",
            target = "Lsmartin/miapi/item/modular/VisualModularItem;isVisualModularItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean creatingstories$allowChainedModuleRandomization(ItemStack stack) {
        return VisualModularItem.isVisualModularItem(stack)
            && !ModularItem.isModularItemNoComponent(stack);
    }

    @Inject(method = "randomizeModuleAndChildren", at = @At("RETURN"), cancellable = true)
    private void creatingstories$randomizeDescendantModules(
        MutableModuleInstance module,
        Material baseline,
        RandomSource random,
        CallbackInfoReturnable<MutableModuleInstance> cir) {
        MutableModuleInstance result = cir.getReturnValue();
        for (Map.Entry<String, MutableModuleInstance> child
            : new ArrayList<>(result.getChildren().entrySet())) {
            result.setChild(child.getKey(),
                randomizeModuleAndChildren(child.getValue(), baseline, random));
        }
        cir.setReturnValue(result);
    }
}
