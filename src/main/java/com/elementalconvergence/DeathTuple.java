package com.elementalconvergence;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class DeathTuple {
    private int timer;
    private BlockPos deathPos;
    private ServerWorld world;

    public DeathTuple(int timer, BlockPos deathPos, ServerWorld world){
        this.timer = timer;
        this.deathPos = deathPos;
        this.world = world;
    }

    public void decrementTimer(){
        if (timer>0) {
            this.timer=this.timer-1;
        }
    }

    public void setTimer(int timer){
        this.timer=timer;
    }

    public int getTimer(){
        return this.timer;
    }

    public void setDeathPos(BlockPos deathPos){
        this.deathPos=deathPos;
    }

    public BlockPos getDeathPos(){
        return this.deathPos;
    }

    public ServerWorld getWorld(){
        return this.world;
    }

    public void setWorld(ServerWorld world){
        this.world = world;
    }


}
