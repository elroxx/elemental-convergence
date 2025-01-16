package com.elementalconvergence;

import net.minecraft.util.math.BlockPos;

public class DeathTuple {
    private int timer;
    private BlockPos deathPos;

    public DeathTuple(int timer, BlockPos deathPos){
        this.timer = timer;
        this.deathPos = deathPos;
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


}
