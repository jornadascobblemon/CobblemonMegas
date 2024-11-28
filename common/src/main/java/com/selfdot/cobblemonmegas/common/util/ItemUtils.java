package com.selfdot.cobblemonmegas.common.util;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import static net.minecraft.component.DataComponentTypes.CUSTOM_DATA;

public class ItemUtils {
    /**
     * Retrieves a copy of the namespaced NBT compound from the given ItemStack.
     *
     * @param itemStack The ItemStack to retrieve the NBT data from.
     * @param namespace The namespace to use for getting the NBT compound.
     * @return A copy of the NbtCompound if present, otherwise an empty compound.
     */
    public static NbtCompound getNbt(ItemStack itemStack, String namespace) {
        NbtComponent component = itemStack.getOrDefault(CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = component.copyNbt();
        return nbt.getCompound(namespace);
    }

    /**
     * Sets a string value in the namespaced NBT data of the given ItemStack.
     * If the NbtCompound previously contained a string for the key, the old value is replaced.
     *
     * @param itemStack The ItemStack to modify.
     * @param namespace The namespace to use for the NBT compound.
     * @param key       The NBT key to set.
     * @param value     The string value to assign.
     */
    public static void setNbtString(ItemStack itemStack, String namespace, String key, String value) {
        itemStack.apply(CUSTOM_DATA, NbtComponent.DEFAULT, current -> {
            NbtCompound newNbt = current.copyNbt();

            if (!newNbt.contains(namespace)) {
                NbtCompound modNbt = new NbtCompound();
                modNbt.putString(key, value);
                newNbt.put(namespace, modNbt);
            } else {
                NbtCompound modNbt = newNbt.getCompound(namespace);
                modNbt.putString(key, value);
                newNbt.put(namespace, modNbt);
            }

            return NbtComponent.of(newNbt);
        });
    }


    /**
     * Sets a boolean value in the namespaced NBT data of the given ItemStack.
     * If the NbtCompound previously contained a boolean for the key, the old value is replaced.
     *
     * @param itemStack The ItemStack to modify.
     * @param namespace The namespace to use for the NBT compound.
     * @param key       The NBT key to set.
     * @param value     The boolean value to assign.
     */
    public static void setNbtBoolean(ItemStack itemStack, String namespace, String key, boolean value) {
        itemStack.apply(CUSTOM_DATA, NbtComponent.DEFAULT, current -> {
            NbtCompound newNbt = current.copyNbt();

            if (!newNbt.contains(namespace)) {
                NbtCompound modNbt = new NbtCompound();
                modNbt.putBoolean(key, value);
                newNbt.put(namespace, modNbt);
            } else {
                NbtCompound modNbt = newNbt.getCompound(namespace);
                modNbt.putBoolean(key, value);
                newNbt.put(namespace, modNbt);
            }

            return NbtComponent.of(newNbt);
        });
    }

    /**
     * Removes a specific key from the namespaced NBT compound of the given ItemStack.
     *
     * @param itemStack The ItemStack to modify.
     * @param namespace The namespace to use for getting the NBT compound.
     * @param key       The NBT key to remove.
     */
    public static void removeNbtKey(ItemStack itemStack, String namespace, String key) {
        itemStack.apply(CUSTOM_DATA, NbtComponent.DEFAULT, current -> {
            NbtCompound newNbt = current.copyNbt();

            if (newNbt.contains(namespace)) {
                NbtCompound modNbt = newNbt.getCompound(namespace);

                if (modNbt.contains(key)) {
                    // Remove the specified key from the namespaced compound if present
                    modNbt.remove(key);

                    if (modNbt.isEmpty()) {
                        newNbt.remove(namespace);
                    } else {
                        newNbt.put(namespace, modNbt);
                    }
                }
            }
            return NbtComponent.of(newNbt);
        });
    }

}
