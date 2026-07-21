package dev.jearchaeologyintegrations.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Prevents Just Enough Archaeology from reusing generated display recipes across
 * integrated-server instances. Those recipes can contain data components backed
 * by a world's reloadable registries (notably enchantment holders on Ancient
 * Books), so sending a cached stack from an old world fails recipe packet encoding.
 */
@Mixin(targets = "cy.jdkdigital.jearchaeology.recipe.Helper", remap = false)
public abstract class HelperRecipeCacheMixin {
    @Shadow(remap = false)
    private static List<RecipeHolder<?>> cachedBrushingRecipes;

    @Shadow(remap = false)
    private static List<RecipeHolder<?>> cachedSniffingRecipes;

    @Inject(method = "getAllBrushingRecipes", at = @At("HEAD"), remap = false)
    private static void creatingStories$discardPreviousServerBrushingRecipes(
        ServerLevel level,
        CallbackInfoReturnable<List<RecipeHolder<?>>> cir
    ) {
        cachedBrushingRecipes.clear();
    }

    @Inject(method = "getAllSniffingRecipes", at = @At("HEAD"), remap = false)
    private static void creatingStories$discardPreviousServerSniffingRecipes(
        ServerLevel level,
        CallbackInfoReturnable<List<RecipeHolder<?>>> cir
    ) {
        cachedSniffingRecipes.clear();
    }
}
