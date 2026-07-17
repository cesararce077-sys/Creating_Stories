package com.creatingstories.workshop.command;

import com.creatingstories.workshop.fusion.FusionResult;
import com.creatingstories.workshop.fusion.ModuleFusion;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

import java.util.ArrayList;
import java.util.List;

public final class FusionCommand {
    private FusionCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("creatingstories")
            .requires(source -> source.hasPermission(2))
            .then(ModularAuditCommand.node())
            .then(Commands.literal("fusion")
                .then(Commands.literal("list")
                    .executes(FusionCommand::listDonorComponents))
                .then(Commands.literal("preview")
                    .then(Commands.argument("component_path", StringArgumentType.string())
                        .executes(context -> execute(context, false))))
                .then(Commands.literal("preview_root")
                    .then(Commands.argument("destination_path", StringArgumentType.string())
                        .executes(context -> executeRootDonor(context, false))))
                .then(Commands.literal("apply")
                    .then(Commands.argument("component_path", StringArgumentType.string())
                        .executes(context -> execute(context, true))))
                .then(Commands.literal("apply_root")
                    .then(Commands.argument("destination_path", StringArgumentType.string())
                        .executes(context -> executeRootDonor(context, true))))));
    }

    private static int listDonorComponents(CommandContext<CommandSourceStack> context) {
        final ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (Exception exception) {
            context.getSource().sendFailure(Component.translatable("commands.creating_stories_workshop.fusion.player_only"));
            return 0;
        }

        ItemStack donor = player.getOffhandItem();
        ModuleInstance root = ItemModule.getModules(donor);
        if (donor.isEmpty() || root.getModule().isEmpty()) {
            context.getSource().sendFailure(Component.translatable("commands.creating_stories_workshop.fusion.need_items"));
            return 0;
        }

        context.getSource().sendSuccess(
            () -> Component.translatable("commands.creating_stories_workshop.fusion.list_header"), false);
        int count = 0;
        for (ModuleInstance part : root.getFlatList()) {
            if (part == root) continue;
            List<String> path = new ArrayList<>();
            part.calculatePosition(path);
            context.getSource().sendSuccess(() -> Component.literal(
                "  " + String.join("/", path) + " -> " + part.moduleId()), false);
            count++;
        }
        return count;
    }

    private static int execute(CommandContext<CommandSourceStack> context, boolean apply) {
        return execute(context, apply, false);
    }

    private static int executeRootDonor(CommandContext<CommandSourceStack> context, boolean apply) {
        return execute(context, apply, true);
    }

    private static int execute(CommandContext<CommandSourceStack> context, boolean apply, boolean rootDonor) {
        final ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (Exception exception) {
            context.getSource().sendFailure(Component.translatable("commands.creating_stories_workshop.fusion.player_only"));
            return 0;
        }

        String path = StringArgumentType.getString(context,
            rootDonor ? "destination_path" : "component_path");
        ItemStack base = player.getMainHandItem();
        ItemStack donor = player.getOffhandItem();
        FusionResult result = rootDonor
            ? ModuleFusion.previewRootDonor(base, donor, path)
            : ModuleFusion.preview(base, donor, path);
        if (!result.valid()) {
            context.getSource().sendFailure(result.message());
            return 0;
        }
        if (!apply) {
            context.getSource().sendSuccess(() -> result.message(), false);
            return 1;
        }
        if (!player.isCreative() && player.experienceLevel < result.levelCost()) {
            context.getSource().sendFailure(Component.translatable(
                "commands.creating_stories_workshop.fusion.need_levels", result.levelCost(), player.experienceLevel));
            return 0;
        }

        // Revalidation above and all mutations occur synchronously on the server thread.
        // Charge and consume only after the output has been constructed successfully.
        if (!player.isCreative()) {
            player.giveExperienceLevels(-result.levelCost());
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, result.output());
        donor.shrink(1);
        player.setItemInHand(InteractionHand.OFF_HAND, donor);
        context.getSource().sendSuccess(() -> Component.translatable(
            "commands.creating_stories_workshop.fusion.success",
            result.output().getHoverName(), path, result.levelCost()), false);
        return 1;
    }
}
