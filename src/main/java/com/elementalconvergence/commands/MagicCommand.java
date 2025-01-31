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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MagicCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("magic")
                .then(CommandManager.argument("index", IntegerArgumentType.integer(-1, ElementalConvergence.BASE_MAGIC_ID.length - 1))
                        .executes(MagicCommand::runSet)));
    }

    private static int runSet(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity sourcePlayer = context.getSource().getPlayer();
            int newIndex = IntegerArgumentType.getInteger(context, "index");

            IMagicDataSaver dataSaver = (IMagicDataSaver) sourcePlayer;
            String message;

            if (dataSaver.getMagicData().getSelectedMagic()==-1){
                dataSaver.getMagicData().setSelectedMagic(newIndex);
                if (newIndex == -1) {
                    message = "You are a still muggle.";
                } else {
                    String magicType = ElementalConvergence.BASE_MAGIC_DISPLAY[newIndex];
                    message = "You feel empowered by " + magicType+" magic.";
                }

            }
            else{
                message = "You can only choose your starter magic once.";
            }




            context.getSource().sendFeedback(() -> Text.of(message), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.of("Error executing command: " + e.getMessage()));
            return -1;
        }
    }
}