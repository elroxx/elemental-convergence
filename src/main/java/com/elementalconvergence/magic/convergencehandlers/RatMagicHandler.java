package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.container.StealInventoryScreenHandler;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.magic.IMagicHandler;
import com.elementalconvergence.magic.MagicRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import com.elementalconvergence.ElementalConvergence;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class RatMagicHandler implements IMagicHandler {
    public static final int RAT_INDEX= (BASE_MAGIC_ID.length-1)+1;
    private boolean hasSkinOn=false;

    public static final String RAT_SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTcxODQ4MTE0MzA0NSwKICAicHJvZmlsZUlkIiA6ICI0NTM1Y2RjNjk3NGU0Nzk4YjljYzY4ODlkZWY1MDk2NiIsCiAgInByb2ZpbGVOYW1lIiA6ICIzZXlyZW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGNmNzgzNjgyMTMzNTY3ZGQ2MzMxY2NjOWU0MGY4YTU3OWYwNGRiZGI2MGM1ZWQzZjZmNDk5MDMyNWUxOTBhYiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
    public static final String RAT_SKIN_SIGNATURE = "o3fF8VutBrzfUb33gZx1uyWTRudKzHlBSSqS7GNLTtj/uYTOgksrmypYvOIZQlYoPcBLcKYqlinfhs6Rmgt8fmHhd2qQAFZnhLWdB8DzE5EMBY2ZkTBQkrve0Bfql9mgExhktvkf6HILcRJGlSMG9lSKSR6g4tJhh1SBi5K3Sr9jLT0jZckrBpFnUejF4kJMI+1GYZeVJsx+OxciSzGw+jZwq65i7gB8yw8YXIl8TDNNXjPjRQ79eBDVafQcVtqe8RMDodOx++3kmFeP3Y3Zgg4Xk7V4Ieli4onmzj2tRBFI0lCLqlR94cwN843c4kkbSxZcUJxeRRDeplfiaS3Aqpv4x8XyQ9pBRZK6FsJ8tDAcKjBYFOVJjtj6DxH8lXnq3VBgSyvEftmjnXY1Aa+te5T99RR8AjAVmBtIGphxoI7JFneNXz4HnDKbcMH39Cq8kHqUVxmyrHLe7KkFGL+dW/13+HlDElJbXFB6aX+rQ56k6YWHVKWe0RUOrlhP7NjwkNc4VpkwrbA/4rphsdWm9Rxs01V4Hn7sF8EQnGVxDcQslg8dbjf5I8SDUPb6nHT1B4kT/K0ohFx8NpD5jbcA8EesCYjKK1cf8SOwrhdtQ3kusGK8VdXzHEUAfupuDsmNW0CBImMo8kSKCoIWTyoDNvhoV912+p6GtvnhJxfpPoQ=";

    public static final float BASE_SCALE=1.0f;
    public static final float BASE_HEALTH=20.0f;

    public static final float RAT_MODE_HEIGHT = 0.12f;
    public static final float RAT_MODE_WIDTH = 0.3f;
    public static final float RAT_MODE_MOTION= 0.5f;
    public static final float RAT_MODE_REACH=0.7f;
    public static final float RAT_MODE_SPEED=2.75f;
    public static final float RAT_MODE_HEALTH=6.0f;

    public static final float RAT_ATTACK=0.01f;


    public static final int RATMODE_DEFAULT_COOLDOWN=10;
    public static int ratModeCooldown=0;
    private boolean ratModeToggle=false;
    //HALF JUMP HEIGHT' NO

    @Override
    public void handleItemRightClick(PlayerEntity player) {
    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int ratLevel = magicData.getMagicLevel(RAT_INDEX);

        if (targetEntity instanceof PlayerEntity targetPlayer && ratModeToggle && ratLevel>=3) {
            //SERVER-SIDE STEAL
            ServerPlayerEntity sourcePlayer = (ServerPlayerEntity) player;
            sourcePlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inventory, p) -> new StealInventoryScreenHandler(syncId, inventory, targetPlayer.getInventory()),
                    Text.literal(targetPlayer.getName().getString() + "'s Inventory")
            ));
        }

        //THIS IS FOR CLIENTSIDE STEALING. USELESS HERE
        //if (world.isClient() && entity instanceof PlayerEntity targetPlayer && player.isSneaking()) {
        // SEND PACKETS ASKING TO OPEN INVENTORY
        //ClientPlayNetworking.send(new OpenInventoryPayload(targetPlayer.getUuid()));
        //return ActionResult.SUCCESS;
        //}
    }

    @Override
    public void handlePassive(PlayerEntity player) {
        if (!hasSkinOn){
            // Set the skin and reload it
            TailoredPlayer tailoredPlayer = (TailoredPlayer) player;
            tailoredPlayer.fabrictailor_setSkin(RAT_SKIN_VALUE, RAT_SKIN_SIGNATURE, true);
            hasSkinOn=true;
        }

        //DEBUFF (aka remove completely attack damage)
        ScaleData playerAttack = ScaleTypes.ATTACK.getScaleData(player);
        ScaleData playerKnockback = ScaleTypes.KNOCKBACK.getScaleData(player);
        if (!(Math.abs(playerAttack.getScale()-RAT_ATTACK)<0.02f)){
            playerAttack.setScale(RAT_ATTACK);
            playerKnockback.setScale(RAT_ATTACK);
        }


        //handle cooldowns
        if (ratModeCooldown>0){
            ratModeCooldown--;
        }
    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {

    }

    @Override
    public void handleKill(PlayerEntity player, Entity victim) {

    }

    @Override
    public void handleMine(PlayerEntity player) {

    }

    @Override
    public void handleBlockBreak(PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {

    }


    @Override
    public void handlePrimarySpell(PlayerEntity player) {
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int ratLevel = magicData.getMagicLevel(RAT_INDEX);
        if (ratLevel>=2 && ratModeCooldown==0) {

            ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
            ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
            ScaleData playerReach = ScaleTypes.BLOCK_REACH.getScaleData(player);
            ScaleData playerEntityReach = ScaleTypes.ENTITY_REACH.getScaleData(player);
            ScaleData playerMotion = ScaleTypes.MOTION.getScaleData(player);


            float heightScale=BASE_SCALE;
            float widthScale=BASE_SCALE;
            float reachScale=BASE_SCALE;
            float motionScale=BASE_SCALE;

            float moveSpeed= MagicRegistry.DEFAULT_MOVE_SPEED;
            float healthScale=BASE_HEALTH;

            if (ratModeToggle){
                ratModeToggle=false;
            }else{
                ratModeToggle=true;
                heightScale=RAT_MODE_HEIGHT;
                widthScale=RAT_MODE_WIDTH;
                reachScale=RAT_MODE_REACH;
                motionScale=RAT_MODE_MOTION;
                moveSpeed=MagicRegistry.DEFAULT_MOVE_SPEED*RAT_MODE_SPEED;
                healthScale=RAT_MODE_HEALTH;
            }

            playerHeight.setScale(heightScale);
            playerWidth.setScale(widthScale);
            playerReach.setScale(reachScale);
            playerEntityReach.setScale(reachScale);
            playerMotion.setScale(motionScale);

            player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(moveSpeed); //Speed fix after motion
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(healthScale);

            ratModeCooldown=RATMODE_DEFAULT_COOLDOWN;
        }

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    public void resetRatSkinToggle(){
        hasSkinOn=false;
    }
}
