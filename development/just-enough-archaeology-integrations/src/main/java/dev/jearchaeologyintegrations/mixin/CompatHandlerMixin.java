package dev.jearchaeologyintegrations.mixin;

import com.mojang.datafixers.util.Pair;
import dev.jearchaeologyintegrations.ArchaeologyMapping;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(targets = "cy.jdkdigital.jearchaeology.compat.CompatHandler", remap = false)
public abstract class CompatHandlerMixin {
    @Inject(method = "getTables", at = @At("RETURN"), remap = false)
    private static void addIntegrationTables(CallbackInfoReturnable<Map<ResourceKey<LootTable>, Pair<String, Ingredient>>> cir) {
        Map<ResourceKey<LootTable>, Pair<String, Ingredient>> tables = cir.getReturnValue();
        for (ArchaeologyMapping mapping : ArchaeologyMapping.ALL) {
            if (!ModList.get().isLoaded(mapping.requiredMod())) {
                continue;
            }
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(mapping.lootTable()));
            Block groundBlock = switch (mapping.ground()) {
                case SAND -> Blocks.SUSPICIOUS_SAND;
                case GRAVEL -> Blocks.SUSPICIOUS_GRAVEL;
                case FOSSILIFEROUS_DIRT -> BuiltInRegistries.BLOCK.get(ResourceLocation.parse("betterarcheology:fossiliferous_dirt"));
            };
            Ingredient ground = Ingredient.of(groundBlock);
            tables.put(key, Pair.of(mapping.locationKey(), ground));
        }
    }
}
