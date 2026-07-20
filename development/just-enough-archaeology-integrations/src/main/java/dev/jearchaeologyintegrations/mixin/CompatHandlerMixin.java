package dev.jearchaeologyintegrations.mixin;

import com.mojang.datafixers.util.Pair;
import dev.jearchaeologyintegrations.ArchaeologyMapping;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
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
            Ingredient ground = Ingredient.of(mapping.ground() == ArchaeologyMapping.Ground.SAND
                ? Blocks.SUSPICIOUS_SAND
                : Blocks.SUSPICIOUS_GRAVEL);
            tables.put(key, Pair.of(mapping.locationKey(), ground));
        }
    }
}
