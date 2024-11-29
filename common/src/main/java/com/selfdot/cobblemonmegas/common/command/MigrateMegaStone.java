package com.selfdot.cobblemonmegas.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.selfdot.cobblemonmegas.common.item.MegaStoneHeldItemManager;
import com.selfdot.cobblemonmegas.common.util.NbtUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class MigrateMegaStone implements Command<ServerCommandSource> {

    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();

        var handStack = player.getMainHandStack();
        // You would think an empty hand would not retain NBT from the last held item, right? WRONG!
        if (handStack.isEmpty()) return error(player, "Hand does not contain valid or old mega stone");

        NbtCompound handStackNbt = NbtUtils.getNbt(handStack, "");
        if (handStackNbt.isEmpty()) return error(player, "Hand does not contain valid or old mega stone");
        if (!handStackNbt.contains("ShowdownID")) return error(player, "Hand does not contain valid or old mega stone");
        var showdownId = handStackNbt.getString("ShowdownID");

        var newMegaStone = MegaStoneHeldItemManager.getInstance().getMegaStoneItem(showdownId);
        if (newMegaStone == ItemStack.EMPTY) return error(player, "Could not migrate mega stone");

        player.getStackInHand(Hand.MAIN_HAND).decrement(1);
        if (player.getInventory().getOccupiedSlotWithRoomForStack(newMegaStone) == -1 && player.getInventory().getEmptySlot() == -1) {
            player.dropStack(newMegaStone);
        }
        else {
            player.giveItemStack(newMegaStone);
        }
        player.sendMessage(Text.literal("Migrated mega stone to new version"));
        return 1;
    }

    private int error(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal(message));
        return 1;
    }

}
