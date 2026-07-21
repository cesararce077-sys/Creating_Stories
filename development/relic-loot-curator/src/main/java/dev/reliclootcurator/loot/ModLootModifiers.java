package dev.reliclootcurator.loot;

import com.mojang.serialization.MapCodec;
import dev.reliclootcurator.RelicLootCurator;
import java.util.function.Supplier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTER =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RelicLootCurator.MOD_ID);

    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> ROUTED_RELIC_LOOT =
            REGISTER.register("routed_relic_loot", () -> CuratedRelicLootModifier.CODEC);

    private ModLootModifiers() {
    }
}
