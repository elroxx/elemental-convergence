package com.elementalconvergence.item;

import com.elementalconvergence.data.BeehiveTeleporter;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.elementalconvergence.magic.convergencehandlers.HoneyMagicHandler.HONEY_INDEX;
import static com.elementalconvergence.world.dimension.ModDimensions.BEEHIVE_DIMENSION_TYPE;
import static com.elementalconvergence.world.dimension.ModDimensions.BEEHIVE_WORLD_KEY;

public class PortableBeehiveItem extends Item {

    public PortableBeehiveItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {

            serverPlayer.getServerWorld().playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_HONEY_BLOCK_PLACE, SoundCategory.PLAYERS,1.0f, 0.01f);

            for (int i = 0; i < 10; i++) {
                double offsetX = world.random.nextGaussian() * 0.1;
                double offsetY = world.random.nextGaussian() * 0.1;
                double offsetZ = world.random.nextGaussian() * 0.1;

                serverPlayer.getServerWorld().spawnParticles(
                        ParticleTypes.FALLING_HONEY,
                        serverPlayer.getX(), serverPlayer.getY() + 0.5, serverPlayer.getZ(),
                        10,
                        offsetX, offsetY, offsetZ,
                        1.0f
                );
            }

            if (serverPlayer.getServerWorld().getRegistryKey().equals(BEEHIVE_WORLD_KEY)) {
                BeehiveTeleporter.exitBeehive(serverPlayer);
            } else {
                IMagicDataSaver dataSaver = (IMagicDataSaver) serverPlayer;
                MagicData magicData = dataSaver.getMagicData();
                int honeyLevel = magicData.getMagicLevel(HONEY_INDEX);

                //lvl 2 ability
                if (honeyLevel>=2 && magicData.getSelectedMagic()==HONEY_INDEX) {
                    BeehiveTeleporter.enterBeehive(serverPlayer, itemStack);
                }
            }

            //playsound + particles hopefully
            serverPlayer.getServerWorld().playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_HONEY_BLOCK_PLACE, SoundCategory.PLAYERS,1.0f, 0.01f);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }
}

