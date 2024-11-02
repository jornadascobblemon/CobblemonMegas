package com.selfdot.cobblemonmegas.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.selfdot.cobblemonmegas.common.item.MegaStoneHeldItemManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import java.util.Collection;

public class GiveMegaStoneCommand implements Command<ServerCommandSource> {

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
        if (players == null) return 0;
        players.forEach(player -> player.giveItemStack(MegaStoneHeldItemManager.getInstance().getMegaStoneItem(
                StringArgumentType.getString(context, "megaStone")
        )));

        return SINGLE_SUCCESS;
    }
}
