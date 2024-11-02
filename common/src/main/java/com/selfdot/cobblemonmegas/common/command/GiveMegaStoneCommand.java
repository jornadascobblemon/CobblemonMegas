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

        String megaStone = StringArgumentType.getString(context, "megaStone").toLowerCase();

        ItemStack itemStack;

//        if (megaStone.equals("redorb")) {
//            itemStack = MegaStoneHeldItemManager.getInstance().getRedOrbItem();
//        } else if (megaStone.equals("blueorb")) {
//            itemStack = MegaStoneHeldItemManager.getInstance().getBlueOrbItem();
//        } else {
        itemStack = MegaStoneHeldItemManager.getInstance().getMegaStoneItem(megaStone);

        players.forEach(player -> player.giveItemStack(itemStack));

        return SINGLE_SUCCESS;
    }
}
