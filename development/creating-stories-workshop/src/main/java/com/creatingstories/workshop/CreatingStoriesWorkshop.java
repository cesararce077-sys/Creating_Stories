package com.creatingstories.workshop;

import com.creatingstories.workshop.command.FusionCommand;
import com.creatingstories.workshop.miapi.OrdinaryToolConverters;
import com.creatingstories.workshop.miapi.SalvagedPartNaming;
import net.minecraft.core.component.DataComponents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.registries.RegistryInventory;
import com.creatingstories.workshop.miapi.PartInstallCrafting;
import com.creatingstories.workshop.enchanting.AncientBookPrintingIntegration;
import net.neoforged.fml.ModList;

@Mod(CreatingStoriesWorkshop.MOD_ID)
public final class CreatingStoriesWorkshop {
    public static final String MOD_ID = "creating_stories_workshop";

    public CreatingStoriesWorkshop(IEventBus modBus) {
        OrdinaryToolConverters.register();
        NeoForge.EVENT_BUS.register(FusionCommand.class);
        NeoForge.EVENT_BUS.addListener(this::serverAboutToStart);
        NeoForge.EVENT_BUS.addListener(this::playerLoggedIn);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            RegistryInventory.registerMiapi(
                RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY,
                PartInstallCrafting.KEY,
                new PartInstallCrafting());
        });
    }

    private void serverAboutToStart(ServerAboutToStartEvent event) {
        if (ModList.get().isLoaded("create_enchantment_industry")
            && ModList.get().isLoaded("immersiveenchanting")) {
            AncientBookPrintingIntegration.register();
        }
    }

    private void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        for (int slot = 0; slot < event.getEntity().getInventory().getContainerSize(); slot++) {
            ItemStack stack = event.getEntity().getInventory().getItem(slot);
            if (stack.is(RegistryInventory.visualOnlymodularItem)
                || SalvagedPartNaming.isWorkshopManagedName(stack.get(DataComponents.CUSTOM_NAME))) {
                ComponentApplyProperty.updateItemStack(
                    stack, event.getEntity().level().registryAccess());
            }
        }
    }
}
