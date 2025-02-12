package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import com.elementalconvergence.ElementalConvergence;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class RatMagicHandler implements IMagicHandler {
    public static final int RAT_INDEX= (BASE_MAGIC_ID.length-1)+1;
    private boolean hasSkinOn=false;

    public static final String RAT_SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTcxODQ4MTE0MzA0NSwKICAicHJvZmlsZUlkIiA6ICI0NTM1Y2RjNjk3NGU0Nzk4YjljYzY4ODlkZWY1MDk2NiIsCiAgInByb2ZpbGVOYW1lIiA6ICIzZXlyZW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGNmNzgzNjgyMTMzNTY3ZGQ2MzMxY2NjOWU0MGY4YTU3OWYwNGRiZGI2MGM1ZWQzZjZmNDk5MDMyNWUxOTBhYiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
    public static final String RAT_SKIN_SIGNATURE = "o3fF8VutBrzfUb33gZx1uyWTRudKzHlBSSqS7GNLTtj/uYTOgksrmypYvOIZQlYoPcBLcKYqlinfhs6Rmgt8fmHhd2qQAFZnhLWdB8DzE5EMBY2ZkTBQkrve0Bfql9mgExhktvkf6HILcRJGlSMG9lSKSR6g4tJhh1SBi5K3Sr9jLT0jZckrBpFnUejF4kJMI+1GYZeVJsx+OxciSzGw+jZwq65i7gB8yw8YXIl8TDNNXjPjRQ79eBDVafQcVtqe8RMDodOx++3kmFeP3Y3Zgg4Xk7V4Ieli4onmzj2tRBFI0lCLqlR94cwN843c4kkbSxZcUJxeRRDeplfiaS3Aqpv4x8XyQ9pBRZK6FsJ8tDAcKjBYFOVJjtj6DxH8lXnq3VBgSyvEftmjnXY1Aa+te5T99RR8AjAVmBtIGphxoI7JFneNXz4HnDKbcMH39Cq8kHqUVxmyrHLe7KkFGL+dW/13+HlDElJbXFB6aX+rQ56k6YWHVKWe0RUOrlhP7NjwkNc4VpkwrbA/4rphsdWm9Rxs01V4Hn7sF8EQnGVxDcQslg8dbjf5I8SDUPb6nHT1B4kT/K0ohFx8NpD5jbcA8EesCYjKK1cf8SOwrhdtQ3kusGK8VdXzHEUAfupuDsmNW0CBImMo8kSKCoIWTyoDNvhoV912+p6GtvnhJxfpPoQ=";

    @Override
    public void handleRightClick(PlayerEntity player) {
    }

    @Override
    public void handlePassive(PlayerEntity player) {
        if (!hasSkinOn){
            // Set the skin and reload it
            TailoredPlayer tailoredPlayer = (TailoredPlayer) player;
            tailoredPlayer.fabrictailor_setSkin(RAT_SKIN_VALUE, RAT_SKIN_SIGNATURE, true);
            hasSkinOn=true;
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

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    public void resetRatSkinToggle(){
        hasSkinOn=false;
    }
}
