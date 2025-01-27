package com.selfdot.cobblemonmegas.common.util;

import com.cobblemon.mod.common.pokemon.Pokemon;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.component.DataComponentTypes.*;

@Slf4j
public class NbtUtils {
    /**
     * Retrieves a copy of the specified namespace's NBT compound from the given ItemStack.
     *
     * @param itemStack The ItemStack from which to extract the NBT data.
     * @param namespace The namespace identifier for the target NBT compound. An empty string indicates the root namespace.
     * @return A copy of the NbtCompound if present, otherwise an empty compound.
     */
    @NotNull
    public static NbtCompound getNbt(@NotNull ItemStack itemStack, @NotNull String namespace) {
        NbtCompound nbt = itemStack.getOrDefault(CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();

        if (namespace.isEmpty()) {
            // root namespace
            return nbt;
        }

        return nbt.getCompound(namespace);
    }

    /**
     * Sets a string value in the namespaced NBT data of the given ItemStack.
     * If the NbtCompound previously contained a string for the key, the old value is replaced.
     *
     * @param itemStack The ItemStack to modify.
     * @param namespace The namespace to use for the NBT compound. An empty string indicates the root namespace.
     * @param key       The NBT key to set.
     * @param value     The string value to assign.
     */
    public static void setNbtString(@NotNull ItemStack itemStack, @NotNull String namespace, @NotNull String key, @NotNull String value) {
        if (key.isEmpty() || value.isEmpty()) {
            log.warn("Invalid NBT data provided to setNbtString. Key: {}, Value: {}", key, value);
            return;
        }

        itemStack.apply(CUSTOM_DATA, NbtComponent.DEFAULT, current -> {
            NbtCompound newNbt = current.copyNbt();

            if (namespace.isEmpty()) {
                // root namespace
                newNbt.putString(key, value);
                return NbtComponent.of(newNbt);
            }

            NbtCompound modNbt = newNbt.getCompound(namespace);
            modNbt.putString(key, value);
            newNbt.put(namespace, modNbt);
            return NbtComponent.of(newNbt);
        });
    }

    /**
     * Sets a boolean value in the namespaced NBT data of the given ItemStack.
     * If the NbtCompound previously contained a boolean for the key, the old value is replaced.
     *
     * @param itemStack The ItemStack to modify.
     * @param namespace The namespace to use for the NBT compound. An empty string indicates the root namespace.
     * @param key       The NBT key to set.
     * @param value     The boolean value to assign.
     */
    public static void setNbtBoolean(@NotNull ItemStack itemStack, @NotNull String namespace, @NotNull String key, @NotNull Boolean value) {
        if (key.isEmpty()) {
            log.warn("Invalid NBT data provided to setNbtBoolean. Key: {}", key);
            return;
        }

        itemStack.apply(CUSTOM_DATA, NbtComponent.DEFAULT, current -> {
            NbtCompound newNbt = current.copyNbt();

            if (namespace.isEmpty()) {
                // root namespace
                newNbt.putBoolean(key, value);
                return NbtComponent.of(newNbt);
            }

            NbtCompound modNbt = newNbt.getCompound(namespace);
            modNbt.putBoolean(key, value);
            newNbt.put(namespace, modNbt);
            return NbtComponent.of(newNbt);
        });
    }

    /**
     * Sets an integer value in the namespaced NBT data of the given ItemStack.
     * If the NbtCompound previously contained an integer for the key, the old value is replaced.
     *
     * @param itemStack The ItemStack to modify.
     * @param namespace The namespace to use for the NBT compound. An empty string indicates the root namespace.
     * @param key       The NBT key to set.
     * @param value     The integer value to assign.
     */
    public static void setNbtInt(@NotNull ItemStack itemStack, @NotNull String namespace, @NotNull String key, @NotNull Integer value) {
        if (key.isEmpty()) {
            log.warn("Invalid NBT data provided to setNbtInt. Key: {}", key);
            return;
        }

        itemStack.apply(CUSTOM_DATA, NbtComponent.DEFAULT, current -> {
            NbtCompound newNbt = current.copyNbt();

            if (namespace.isEmpty()) {
                // root namespace
                newNbt.putInt(key, value);
                return NbtComponent.of(newNbt);
            }

            NbtCompound modNbt = newNbt.getCompound(namespace);
            modNbt.putInt(key, value);
            newNbt.put(namespace, modNbt);
            return NbtComponent.of(newNbt);
        });
    }

    /**
     * Removes a specific key from the namespaced NBT compound of the given ItemStack.
     *
     * @param itemStack The ItemStack to modify.
     * @param namespace The namespace to use for getting the NBT compound. An empty string indicates the root namespace.
     * @param key       The NBT key to remove.
     */
    public static void removeNbtKey(@NotNull ItemStack itemStack, @NotNull String namespace, @NotNull String key) {
        if (key.isEmpty()) {
            log.warn("Invalid NBT data provided to removeNbtKey. Key: {}", key);
            return;
        }

        itemStack.apply(CUSTOM_DATA, NbtComponent.DEFAULT, current -> {
            NbtCompound newNbt = current.copyNbt();

            if (namespace.isEmpty()) {
                // root namespace
                newNbt.remove(key);
                return NbtComponent.of(newNbt);
            }

            NbtCompound modNbt = newNbt.getCompound(namespace);
            modNbt.remove(key);
            newNbt.put(namespace, modNbt);
            return NbtComponent.of(newNbt);
        });
    }

    /**
     * Sets the italic style of an item's name in an ItemStack.
     *
     * @param itemStack the ItemStack to modify
     * @param value     true to set the item name to italic, false otherwise
     */
    public static void setItemName(@NotNull ItemStack itemStack, @NotNull String value, @NotNull Boolean withItalic) {
        itemStack.apply(ITEM_NAME, Text.of("KeyStone"), current -> {
            MutableText currentText = current.copy();
            Style textStyle = currentText.getStyle().withItalic(withItalic);

            return MutableText.of(PlainTextContent.EMPTY).append(value).setStyle(textStyle);
        });
    }

    /**
     * Sets a string value in the persistent NBT data of the given Pokémon.
     *
     * @param pokemon the Pokémon to modify
     * @param key     the NBT key to set
     * @param value   the string value to assign
     */
    public static void setPokemonNbtString(@NotNull Pokemon pokemon, @NotNull String key, @NotNull String value) {
        pokemon.getPersistentData().putString(key, value);
        // we have to emit to update the persistent data
        pokemon.getAnyChangeObservable().emit();
    }

    /**
     * Removes a string value from the persistent NBT data of the given Pokémon.
     *
     * @param pokemon the Pokémon to modify
     * @param key     the NBT key to remove
     */
    public static void removePokemonNbtString(@NotNull Pokemon pokemon, @NotNull String key) {
        pokemon.getPersistentData().remove(key);
        // we have to emit to update the persistent data
        pokemon.getAnyChangeObservable().emit();
    }

    /**
     * Sets an integer value in the custom model data of the given ItemStack.
     *
     * @param itemStack The ItemStack to modify.
     * @param value     The integer value to assign.
     */
    public static void setCustomModelData(@NotNull ItemStack itemStack, @NotNull Integer value) {
        itemStack.apply(CUSTOM_MODEL_DATA, CustomModelDataComponent.DEFAULT, current -> new CustomModelDataComponent(value));
    }

}
