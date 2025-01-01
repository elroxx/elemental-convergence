package com.elementalconvergence.commands;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.IMagicDataSaver;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

public class GetSelectedMagicCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("selectedmagic")
                .requires(source -> source.hasPermissionLevel(2)) // Requires OP level 2
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.literal("get")
                                .executes(GetSelectedMagicCommand::runGet))
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("index", IntegerArgumentType.integer(-1, ElementalConvergence.BASE_MAGIC_ID.length - 1))
                                        .executes(GetSelectedMagicCommand::runSet)))));
    }

    private static int runGet(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
            IMagicDataSaver dataSaver = (IMagicDataSaver) targetPlayer;
            int selectedMagic = dataSaver.getMagicData().getSelectedMagic();

            String message;
            if (selectedMagic == -1) {
                message = targetPlayer.getName().getString() + " has no magic type currently selected";
            } else {
                String magicType = ElementalConvergence.BASE_MAGIC_DISPLAY[selectedMagic];
                message = targetPlayer.getName().getString() + "'s currently selected magic: " + magicType;
            }

            context.getSource().sendFeedback(() -> Text.of(message), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.of("Error executing command: " + e.getMessage()));
            return -1;
        }
    }

    private static int runSet(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
            ServerPlayerEntity sourcePlayer = context.getSource().getPlayer();
            int newIndex = IntegerArgumentType.getInteger(context, "index");

            IMagicDataSaver dataSaver = (IMagicDataSaver) targetPlayer;
            dataSaver.getMagicData().setSelectedMagic(newIndex);

            String message;
            if (newIndex == -1) {
                message = "Cleared " + targetPlayer.getName().getString() + "'s selected magic";
            } else {
                String magicType = ElementalConvergence.BASE_MAGIC_DISPLAY[newIndex];
                message = "Set " + targetPlayer.getName().getString() + "'s selected magic to " + magicType;
            }

            // Only send one message - if you're targeting yourself, you'll get it as the source
            if (sourcePlayer != targetPlayer) {
                targetPlayer.sendMessage(Text.of(message));
            }
            context.getSource().sendFeedback(() -> Text.of(message), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.of("Error executing command: " + e.getMessage()));
            return -1;
        }
    }
}