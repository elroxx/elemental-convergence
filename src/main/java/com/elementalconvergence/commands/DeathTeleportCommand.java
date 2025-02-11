package com.elementalconvergence.commands;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.magic.handlers.DeathMagicHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import static com.elementalconvergence.ElementalConvergence.deathMap;

public class DeathTeleportCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("deathteleport")
                .then(CommandManager.argument("playerName", StringArgumentType.string())
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    String deathName = StringArgumentType.getString(context, "playerName");

                    IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                    MagicData magicData = dataSaver.getMagicData();
                    int deathLevel = magicData.getMagicLevel(DeathMagicHandler.DEATH_INDEX);
                    if (deathLevel>=3 && magicData.getSelectedMagic()==DeathMagicHandler.DEATH_INDEX){
                        //NEED TO ADD TELEPORTATION LOGIC HERE.
                        if (deathMap.get(deathName)!=null) {
                            if (!deathName.equals(player.getName().getString())){
                                BlockPos deathPos = deathMap.get(deathName).getDeathPos();
                                ServerWorld deathWorld = deathMap.get(deathName).getWorld();
                                //Teleport caster to the deathPos
                                player.teleport((ServerWorld) player.getWorld(), deathPos.getX()+0.5, deathPos.getY(), deathPos.getZ()+0.5, player.getYaw(), player.getPitch());
                                deathMap.get(deathName).setTimer(0);
                                player.getWorld().playSound(
                                        null, //So that everybody hears it
                                        player.getX(),
                                        player.getY(),
                                        player.getZ(),
                                        SoundEvents.ENTITY_FOX_TELEPORT,
                                        SoundCategory.MASTER,
                                        1.0f,
                                        1.0f
                                );
                            }else{
                                player.sendMessage(Text.of("Cannot teleport to your own corpse"));
                            }
                            return 1;
                        }
                    }
                    return -1;
                })));
    }

}
