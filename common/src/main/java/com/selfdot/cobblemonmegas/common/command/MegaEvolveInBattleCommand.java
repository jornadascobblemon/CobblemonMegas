package com.selfdot.cobblemonmegas.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.selfdot.cobblemonmegas.common.util.MegaUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class MegaEvolveInBattleCommand implements Command<ServerCommandSource> {

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;
        if (!MegaUtils.attemptMegaEvolveInBattle(player, true)) return -1;
        return SINGLE_SUCCESS;
    }

}
