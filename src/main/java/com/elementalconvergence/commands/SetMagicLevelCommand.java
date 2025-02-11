package com.elementalconvergence.commands;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.IMagicDataSaver;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
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
                .then(CommandManager.argument("index", IntegerArgumentType.integer(0, ElementalConvergence.FULL_MAGIC_ID.length - 1))
                        .then(CommandManager.argument("level", IntegerArgumentType.integer(0))
                                .executes(SetMagicLevelCommand::run))));
    }

    private static int run(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        int index = IntegerArgumentType.getInteger(context, "index");
        int level = IntegerArgumentType.getInteger(context, "level");

        if (player != null) {
            IMagicDataSaver dataSaver = (IMagicDataSaver) player;
            dataSaver.getMagicData().setMagicLevel(index, level);

            String magicType = ElementalConvergence.FULL_MAGIC_DISPLAY[index];
            player.sendMessage(Text.of("Set " + magicType + " magic level to " + level));
            return 1;
        }
        return -1;
    }
}