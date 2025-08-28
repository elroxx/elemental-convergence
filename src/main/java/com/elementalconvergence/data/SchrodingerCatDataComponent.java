package com.elementalconvergence.data;

import com.elementalconvergence.ElementalConvergence;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public class SchrodingerCatDataComponent {
    public static final int INVENTORY_SIZE = 27; // Same as single chest

    private static AttachmentType<CatInventoryData> CAT_INVENTORY_ATTACHMENT;

    public static void register() {
        CAT_INVENTORY_ATTACHMENT = AttachmentRegistry.<CatInventoryData>builder()
                .initializer(() -> new CatInventoryData(DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY)))
                .persistent(CatInventoryData.CODEC)
                .buildAndRegister(ElementalConvergence.id("cat_inventory"));
    }

    public static DefaultedList<ItemStack> getCatInventory(PlayerEntity player) {
        CatInventoryData data = player.getAttachedOrCreate(CAT_INVENTORY_ATTACHMENT);
        return data.inventory();
    }

    public static void setCatInventory(PlayerEntity player, DefaultedList<ItemStack> inventory) {
        CatInventoryData data = new CatInventoryData(inventory);
        player.setAttached(CAT_INVENTORY_ATTACHMENT, data);
    }

    public record CatInventoryData(DefaultedList<ItemStack> inventory) {
        public static final Codec<CatInventoryData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ItemStack.CODEC.listOf().fieldOf("inventory").forGetter(data -> {
                            List<ItemStack> list = new ArrayList<>();
                            for (ItemStack stack : data.inventory()) {
                                list.add(stack);
                            }
                            return list;
                        })
                ).apply(instance, itemStacks -> {
                    DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
                    for (int i = 0; i < Math.min(itemStacks.size(), inventory.size()); i++) {
                        inventory.set(i, itemStacks.get(i));
                    }
                    return new CatInventoryData(inventory);
                })
        );
    }
}
