package com.creatingstories.workshop.mixin;

import com.creatingstories.workshop.miapi.EquipmentCompatibilityPolicy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.ModularItem;

/** Prevents Armory's generated converters from stripping behavior from signature equipment. */
@Mixin(value = ModularItemStackConverter.class, remap = false)
public abstract class ModularItemStackConverterMixin {
    @Inject(method = "getModularVersion", at = @At("HEAD"), cancellable = true)
    private static void creatingstories$protectSpecialEquipment(
        ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.isEmpty() || ModularItem.isModularItem(stack)) return;

        EquipmentCompatibilityPolicy.Kind kind;
        if (stack.getItem() instanceof ArmorItem) {
            kind = EquipmentCompatibilityPolicy.Kind.ARMOR;
        } else if (stack.getItem() instanceof BowItem
            || stack.getItem() instanceof CrossbowItem
            || stack.getItem() instanceof ArrowItem) {
            kind = EquipmentCompatibilityPolicy.Kind.RANGED;
        } else {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!id.equals(ResourceLocation.withDefaultNamespace("elytra"))) return;
            kind = EquipmentCompatibilityPolicy.Kind.ARMOR;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (EquipmentCompatibilityPolicy.classify(id, kind)
            != EquipmentCompatibilityPolicy.Classification.ORDINARY) {
            cir.setReturnValue(stack);
        }
    }
}
