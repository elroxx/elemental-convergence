package com.elementalconvergence.networking;

import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskScheduler {
    private static final Map<ServerWorld, List<Runnable>> NEXT_TICK_TASKS = new HashMap<>();

    public static void runNextTick(ServerWorld world, Runnable task) {
        NEXT_TICK_TASKS.computeIfAbsent(world, w -> new ArrayList<>()).add(task);
    }

    public static void tick(ServerWorld world) {
        List<Runnable> tasks = NEXT_TICK_TASKS.remove(world);
        if (tasks != null) {
            tasks.forEach(Runnable::run);
        }
    }
}
