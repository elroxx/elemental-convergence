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

public class SetMagicLevelCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("magiclevel")
                .requires(source -> source.hasPermissionLevel(2)) // Requires OP level 2
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("index", IntegerArgumentType.integer(0, ElementalConvergence.FULL_MAGIC_ID.length - 1))
                                        .then(CommandManager.argument("level", IntegerArgumentType.integer(0))
                                                .executes(SetMagicLevelCommand::runSet)))))
                .then(CommandManager.literal("get")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.argument("index", IntegerArgumentType.integer(0, ElementalConvergence.FULL_MAGIC_ID.length - 1))
                                        .executes(SetMagicLevelCommand::runGet)))));
    }

    private static int runSet(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            int index = IntegerArgumentType.getInteger(context, "index");
            int level = IntegerArgumentType.getInteger(context, "level");

            IMagicDataSaver dataSaver = (IMagicDataSaver) player;
            dataSaver.getMagicData().setMagicLevel(index, level);

            String magicType = ElementalConvergence.FULL_MAGIC_DISPLAY[index];
            context.getSource().sendMessage(Text.of("Set " + player.getName().getString() + "'s " + magicType + " magic level to " + level));
            return 1;
        } catch (Exception e) {
            context.getSource().sendMessage(Text.of("Failed to set magic level"));
            return -1;
        }
    }

    private static int runGet(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            int index = IntegerArgumentType.getInteger(context, "index");

            IMagicDataSaver dataSaver = (IMagicDataSaver) player;
            int level = dataSaver.getMagicData().getMagicLevel(index);

            String magicType = ElementalConvergence.FULL_MAGIC_DISPLAY[index];
            context.getSource().sendMessage(Text.of(player.getName().getString() + "'s " + magicType + " magic level is " + level));
            return 1;
        } catch (Exception e) {
            context.getSource().sendMessage(Text.of("Failed to get magic level"));
            return -1;
        }
    }
}