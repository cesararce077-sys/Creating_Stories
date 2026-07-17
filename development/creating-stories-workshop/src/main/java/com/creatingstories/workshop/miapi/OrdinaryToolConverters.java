package com.creatingstories.workshop.miapi;

import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.ItemToModularConverter;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

/** Adds conservative converters for mundane modded material tiers discovered by MIAPI. */
public final class OrdinaryToolConverters {
    private static final ResourceLocation MATERIAL = ResourceLocation.fromNamespaceAndPath("miapi", "material");
    private static final String WOOD = "miapi:wood/wood";
    private static final Map<String, ItemStack> PACK_CONVERTERS = new ConcurrentHashMap<>();
    private static boolean registered;

    private OrdinaryToolConverters() {}

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        // MIAPI exposes this hook through its bundled Architectury/Nucleus event API.
        // Those implementation libraries are jar-in-jar runtime dependencies, so use
        // reflection here instead of forcing duplicate loadable copies into the pack.
        try {
            Class<?> events = Class.forName("smartin.miapi.events.MiapiEvents");
            Class<?> listenerType = Class.forName("smartin.miapi.events.MiapiEvents$CreateMaterialModularConvertersEvent");
            Object listener = Proxy.newProxyInstance(listenerType.getClassLoader(), new Class<?>[]{listenerType},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            case "toString" -> "CreatingStoriesMaterialConverterListener";
                            default -> throw new UnsupportedOperationException(method.getName());
                        };
                    }
                    if (method.getName().equals("generated")) {
                        @SuppressWarnings("unchecked") List<TieredItem> tools = (List<TieredItem>) args[1];
                        addForMaterial((Material) args[0], tools);
                        Class<?> result = Class.forName("dev.architectury.event.EventResult");
                        return result.getMethod("pass").invoke(null);
                    }
                    return null;
                });
            Object event = events.getField("GENERATE_MATERIAL_CONVERTERS").get(null);
            // PrioritizedEvent<T>.register erases its generic parameter to Object.
            event.getClass().getMethod("register", Object.class).invoke(event, listener);
            // Integrated-server reloads clear the shared converter map after the
            // client generated-material pass. Restore our pack-owned entries at END.
            ReloadEvents.END.subscribe((isClient, registryAccess, worker) -> {
                addPredefinedFamily("iceandfire", "copper", "miapi:metal/copper");
                addPredefinedFamily("create_sa", "copper", "miapi:metal/copper");
                addPredefinedFamily("create_sa", "zinc", "creating_stories_workshop:metal/zinc");
                addPredefinedFamily("create_sa", "brass", "creating_stories_workshop:metal/brass");
                addPredefinedFamily("create_sa", "rose_quartz", "creating_stories_workshop:crystal/rose_quartz");
                ItemToModularConverter.regexes.putAll(PACK_CONVERTERS);
                Miapi.LOGGER.info("Creating Stories restored {} modular converters after {} reload",
                    PACK_CONVERTERS.size(), isClient ? "client" : "server");
            });
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to register MIAPI material converter hook", exception);
        }
    }

    static void addForMaterial(Material material, List<TieredItem> tools) {
        for (TieredItem tool : tools) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(tool);
            if (!ToolCompatibilityPolicy.isOrdinary(id.getNamespace(), id.getPath()) || hasConverter(id)) continue;
            ModuleInstance root = tree(ToolCompatibilityPolicy.toolType(id.getPath()), material.getID().toString());
            if (root == null) continue;
            ItemStack output = new ItemStack(RegistryInventory.modularItem);
            root.writeToItem(output);
            String regex = Pattern.quote(id.toString());
            PACK_CONVERTERS.put(regex, output);
            ItemToModularConverter.regexes.put(regex, output);
            Miapi.LOGGER.info("Creating Stories added modular converter for {} using {}", id, material.getID());
        }
    }

    /** Predefined MIAPI materials do not fire the generated-material converter event. */
    private static void addPredefinedFamily(String namespace, String materialName, String materialId) {
        for (String type : List.of("sword", "pickaxe", "axe", "shovel", "hoe")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, materialName + "_" + type);
            if (!BuiltInRegistries.ITEM.containsKey(id)) continue;
            ModuleInstance root = tree(type, materialId);
            ItemStack output = new ItemStack(RegistryInventory.modularItem);
            root.writeToItem(output);
            String regex = Pattern.quote(id.toString());
            PACK_CONVERTERS.put(regex, output);
            Miapi.LOGGER.info("Creating Stories added predefined modular converter for {} using {}", id, materialId);
        }
    }


    public static boolean hasConverter(ResourceLocation id) {
        String value = id.toString();
        return ItemToModularConverter.regexes.keySet().stream().anyMatch(value::matches);
    }

    private static ModuleInstance tree(String type, String material) {
        return switch (type) {
            case "sword" -> module("tm_arsenal:handle/sword", WOOD, Map.of(
                "guard", module("tm_arsenal:guard/normal", material, Map.of(
                    "blade", module("tm_arsenal:blade/normal", material, Map.of()))),
                "pommel", module("tm_arsenal:pommel/round", material, Map.of())));
            case "pickaxe" -> toolTree("tm_arsenal:tool/pickaxe_front", "tm_arsenal:tool/pickaxe_back", material);
            case "axe" -> toolTree("tm_arsenal:tool/axe_front", "tm_arsenal:tool/tool_back", material);
            case "hoe" -> toolTree("tm_arsenal:tool/hoe_front", "tm_arsenal:tool/tool_back", material);
            case "shovel" -> module("tm_arsenal:handle/tool", WOOD, Map.of(
                "guard", module("tm_arsenal:guard/tool_adapter", material, Map.of(
                    "tool_head", module("tm_arsenal:tool/shovel", material, Map.of())))));
            default -> null;
        };
    }

    private static ModuleInstance toolTree(String front, String back, String material) {
        return module("tm_arsenal:handle/tool", WOOD, Map.of(
            "guard", module("tm_arsenal:guard/tool_adapter", material, Map.of(
                "tool_head", module(front, material, Map.of(
                    "tool_back", module(back, material, Map.of())))))));
    }

    private static ModuleInstance module(String id, String material, Map<String, ModuleInstance> children) {
        return new ModuleInstance(ResourceLocation.parse(id),
            Map.of(MATERIAL, new JsonPrimitive(material)), children, Miapi.registryAccess);
    }
}
