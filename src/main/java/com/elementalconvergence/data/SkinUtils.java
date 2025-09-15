package com.elementalconvergence.data;

import net.minecraft.entity.player.PlayerEntity;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.util.SkinFetcher;

// Your new optimized skin reset methods
public class SkinUtils {

    /**
     * Fetches and stores the original skin data for a player (only call this once per player)
     */
    public static void fetchAndStoreOriginalSkin(PlayerEntity player) {
        IOriginalSkinDataSaver skinDataSaver = (IOriginalSkinDataSaver) player;
        OriginalSkinData skinData = skinDataSaver.getOriginalSkinData();

        // Only fetch if we haven't already done so
        if (!skinData.hasFetchedOnce()) {
            String name = player.getDisplayName().getString();

            // This is the only place where we do the slow network fetch
            var fetchedSkin = SkinFetcher.fetchSkinByName(name);

            if (fetchedSkin !=null && fetchedSkin.value() != null && fetchedSkin.signature() != null) {
                skinData.setSkinData(fetchedSkin.value(), fetchedSkin.signature());
                // NBT will be automatically saved when the player data is saved
            }
        }
    }

    /**
     * Fast skin reset using cached NBT data
     */
    public static void resetPlayerSkin(PlayerEntity player) {
        IOriginalSkinDataSaver skinDataSaver = (IOriginalSkinDataSaver) player;
        OriginalSkinData skinData = skinDataSaver.getOriginalSkinData();

        // If we don't have cached skin data, fetch it first
        if (!skinData.hasValidSkinData()) {
            fetchAndStoreOriginalSkin(player);
        }

        // Now use the cached data for fast reset
        if (skinData.hasValidSkinData()) {
            TailoredPlayer tailoredPlayer = (TailoredPlayer) player;
            tailoredPlayer.fabrictailor_setSkin(
                    skinData.getOriginalSkinValue(),
                    skinData.getOriginalSkinSignature(),
                    true
            );
        }
    }

    public static void onPlayerJoin(PlayerEntity player) {
        // Fetch and store original skin data in the background
        // You can call this in your player join event handler
        fetchAndStoreOriginalSkin(player);
    }
}