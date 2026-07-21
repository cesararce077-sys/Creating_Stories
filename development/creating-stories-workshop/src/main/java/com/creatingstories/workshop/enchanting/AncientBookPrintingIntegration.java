package com.creatingstories.workshop.enchanting;

import com.mojang.serialization.DataResult;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.EnchantedBookPrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviour;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/** Makes Immersive Enchanting's discovered Ancient Books renewable through CEI's normal printer loop. */
public final class AncientBookPrintingIntegration {
    private static final ResourceLocation ANCIENT_BOOK_ID = ResourceLocation.fromNamespaceAndPath(
        "immersiveenchanting", "ancient_book");
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    private AncientBookPrintingIntegration() {}

    public static void register() {
        if (REGISTERED.compareAndSet(false, true)) {
            PrintingBehaviour.register(AncientBookPrintingIntegration::create);
        }
    }

    private static Optional<DataResult<PrintingBehaviour>> create(Level level,
                                                                   SmartFluidTankBehaviour tank,
                                                                   ItemStack template) {
        Item ancientBook = BuiltInRegistries.ITEM.get(ANCIENT_BOOK_ID);
        if (!template.is(ancientBook)) return Optional.empty();

        ItemStack vanillaProxy = template.transmuteCopy(Items.ENCHANTED_BOOK, 1);
        return EnchantedBookPrintingBehaviour.create(level, tank, vanillaProxy)
            .map(result -> result.map(delegate -> new AncientBookPrintingBehaviour(delegate, ancientBook)));
    }

    private record AncientBookPrintingBehaviour(PrintingBehaviour delegate, Item ancientBook)
        implements PrintingBehaviour {

        @Override
        public boolean isValid() {
            return delegate.isValid();
        }

        @Override
        public boolean isSafeNBT() {
            return delegate.isSafeNBT();
        }

        @Override
        public int getRequiredItemCount(Level level, ItemStack stack) {
            return delegate.getRequiredItemCount(level, stack);
        }

        @Override
        public int getRequiredFluidAmount(Level level, ItemStack stack, FluidStack fluid) {
            return delegate.getRequiredFluidAmount(level, stack, fluid);
        }

        @Override
        public ItemStack getResult(Level level, ItemStack stack, FluidStack fluid) {
            return delegate.getResult(level, stack, fluid).transmuteCopy(ancientBook, 1);
        }

        @Override
        public void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer) {
            delegate.onFinished(level, pos, printer);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
            return delegate.addToGoggleTooltip(tooltip, sneaking);
        }
    }
}
