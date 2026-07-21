package dev.reliclootcurator;

import dev.reliclootcurator.config.CuratorConfigManager;
import dev.reliclootcurator.loot.CuratedRelicRouter;
import dev.reliclootcurator.loot.ModLootModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@Mod(RelicLootCurator.MOD_ID)
public final class RelicLootCurator {
    public static final String MOD_ID = "relic_loot_curator";

    public RelicLootCurator(IEventBus modBus) {
        ModLootModifiers.REGISTER.register(modBus);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);
    }

    private void onServerStarted(ServerStartedEvent event) {
        CuratorConfigManager.reload();
        CuratedRelicRouter.rebuild();
    }

    private void onServerStopped(ServerStoppedEvent event) {
        CuratedRelicRouter.clear();
    }
}
