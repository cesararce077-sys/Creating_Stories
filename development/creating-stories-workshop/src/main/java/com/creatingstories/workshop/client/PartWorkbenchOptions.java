package com.creatingstories.workshop.client;

import com.creatingstories.workshop.CreatingStoriesWorkshop;
import com.creatingstories.workshop.miapi.PartInstallCrafting;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import smartin.miapi.client.gui.crafting.crafter.replace.CraftOption;
import smartin.miapi.client.gui.crafting.crafter.replace.ReplaceView;
import smartin.miapi.material.properties.AllowedMaterial;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = CreatingStoriesWorkshop.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PartWorkbenchOptions {
    private PartWorkbenchOptions() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ReplaceView.optionSuppliers.add(context -> {
            List<CraftOption> options = new ArrayList<>();
            ItemStack target = context.getItemstack();
            for (Slot inventorySlot : context.getScreenHandler().slots) {
                if (inventorySlot.getItem().isEmpty()) continue;
                if (inventorySlot.container == context.getScreenHandler().inventory
                    && inventorySlot.getContainerSlot() == 0) continue;

                ModuleInstance donor = ItemModule.getModules(inventorySlot.getItem());
                if (donor.getModule().isEmpty() || !context.getSlot().allowedIn(donor)) continue;

                int sourceIndex = inventorySlot.index;
                Map<ResourceLocation, JsonElement> data = new HashMap<>();
                data.put(PartInstallCrafting.KEY, new JsonPrimitive(sourceIndex));
                data.put(AllowedMaterial.KEY, new JsonPrimitive(false));
                Component name = Component.translatable(
                    "creating_stories_workshop.workbench.use_part", inventorySlot.getItem().getHoverName());
                options.add(new CraftOption(donor.getModule(), () -> data, -300, name));
            }
            return options;
        }));
    }
}
