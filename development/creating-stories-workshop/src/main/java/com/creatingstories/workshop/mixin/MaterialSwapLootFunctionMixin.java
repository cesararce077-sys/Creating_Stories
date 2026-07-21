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
import smartin.miapi.loot.MaterialSwapLootFunction;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.MutableModuleInstance;

import java.util.ArrayList;
import java.util.Map;

/**
 * MIAPI 2.3.8 only rolls the root module despite this method's name. Apply the
 * same configured material roll to each installed descendant so generated
 * equipment can contain genuinely mixed materials.
 */
@Mixin(value = MaterialSwapLootFunction.class, remap = false)
public abstract class MaterialSwapLootFunctionMixin {
    @Shadow
    abstract MutableModuleInstance randomizeMaterialAndChildren(
        MutableModuleInstance module, Material baseline, RandomSource random);

    /**
     * Loot functions are intentionally chained. MIAPI's original guard rejects
     * every already-modular stack, so a material swap placed after a module
     * swap can never run. Only reject visual-only salvage/broken containers;
     * functional modular equipment remains eligible for the next loot pass.
     */
    @Redirect(
        method = "apply",
        at = @At(
            value = "INVOKE",
            target = "Lsmartin/miapi/item/modular/VisualModularItem;isVisualModularItem(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean creatingstories$allowChainedMaterialRandomization(ItemStack stack) {
        return VisualModularItem.isVisualModularItem(stack)
            && !ModularItem.isModularItemNoComponent(stack);
    }

    @Inject(method = "randomizeMaterialAndChildren", at = @At("RETURN"), cancellable = true)
    private void creatingstories$randomizeDescendantMaterials(
        MutableModuleInstance module,
        Material baseline,
        RandomSource random,
        CallbackInfoReturnable<MutableModuleInstance> cir) {
        MutableModuleInstance result = cir.getReturnValue();
        for (Map.Entry<String, MutableModuleInstance> child
            : new ArrayList<>(result.getChildren().entrySet())) {
            result.setChild(child.getKey(),
                randomizeMaterialAndChildren(child.getValue(), baseline, random));
        }
        cir.setReturnValue(result);
    }
}
