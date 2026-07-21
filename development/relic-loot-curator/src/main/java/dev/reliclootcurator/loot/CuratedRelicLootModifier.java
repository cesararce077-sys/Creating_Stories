package dev.reliclootcurator.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class CuratedRelicLootModifier extends LootModifier {
    public static final MapCodec<CuratedRelicLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, CuratedRelicLootModifier::new));

    public CuratedRelicLootModifier(net.minecraft.world.level.storage.loot.predicates.LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        CuratedRelicRouter.RouterState router = CuratedRelicRouter.state();
        if (generatedLoot.stream().anyMatch(stack -> router.isCuratedLootItem(stack.getItem()))) {
            return generatedLoot;
        }

        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        if (entity == null || origin == null) {
            return generatedLoot;
        }

        Level level = entity.level();
        ResourceLocation tableId = context.getQueriedLootTableId();
        String table = tableId.toString();
        String dimension = level.dimension().location().toString();
        String biome = level.getBiome(BlockPos.containing(origin)).getRegisteredName();

        List<CuratedRelicRouter.ThemedCandidate> themed = router.matchingThemed(dimension, biome, table);
        boolean fallbackEligible = router.fallbackEligible(dimension, table);
        RandomSource random = context.getRandom();

        RoutingPolicy.Decision decision = RoutingPolicy.decide(
                !themed.isEmpty(),
                router.themedChance(),
                random.nextDouble(),
                fallbackEligible,
                router.fallbackChance(),
                random.nextDouble()
        );

        Item selected = switch (decision) {
            case THEMED -> router.chooseThemed(themed, random);
            case FALLBACK -> router.chooseFallback(random);
            case NONE -> null;
        };
        if (selected != null) {
            generatedLoot.add(selected.getDefaultInstance());
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ROUTED_RELIC_LOOT.get();
    }
}
