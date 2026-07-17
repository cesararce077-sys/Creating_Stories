package com.creatingstories.workshop;

import com.creatingstories.workshop.command.FusionCommand;
import com.creatingstories.workshop.miapi.OrdinaryToolConverters;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import smartin.miapi.registries.RegistryInventory;
import com.creatingstories.workshop.miapi.PartInstallCrafting;

@Mod(CreatingStoriesWorkshop.MOD_ID)
public final class CreatingStoriesWorkshop {
    public static final String MOD_ID = "creating_stories_workshop";

    public CreatingStoriesWorkshop(IEventBus modBus) {
        OrdinaryToolConverters.register();
        NeoForge.EVENT_BUS.register(FusionCommand.class);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> RegistryInventory.registerMiapi(
            RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY,
            PartInstallCrafting.KEY,
            new PartInstallCrafting()));
    }
}
