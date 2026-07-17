package com.creatingstories.workshop.fusion;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record FusionResult(boolean valid, ItemStack output, int levelCost, Component message) {
    public static FusionResult failure(Component message) {
        return new FusionResult(false, ItemStack.EMPTY, 0, message);
    }
}
