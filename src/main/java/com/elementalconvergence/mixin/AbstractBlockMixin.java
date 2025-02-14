package com.elementalconvergence.mixin;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.elementalconvergence.magic.handlers.LightMagicHandler.LIGHT_INDEX;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionShape(BlockState state, BlockView world, BlockPos pos,
                                     ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        //THIS MIGHT BE THE LAGGIEST THING YET??!??
        //first check if opaque to remove as many checks as possible
        if (!state.isOpaque()) {
            // Only proceed if this is an entity context
            if (context instanceof EntityShapeContext entityContext) {
                //VERIFYING TOO MANY THINGS
                //System.out.println("entity");
                Entity entity = entityContext.getEntity();
                if (entity instanceof ServerPlayerEntity player) {
                    IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                    MagicData magicData = dataSaver.getMagicData();
                    int selectedMagic=magicData.getSelectedMagic();
                    System.out.println(selectedMagic);
                    //PROBLEM: I GET FOR SOME REASON A LOT OF -1
                    // THE -1 ARE THE CLIENTPLAYERENTITY
                    //I NEED TO PUT THE magicData.getSelectedMagic() there too!. never have to use it tho
                    if (magicData.getSelectedMagic()==5) {
                        cir.setReturnValue(VoxelShapes.empty());
                    }
                }
            }
        }
    }

}
