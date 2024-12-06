package com.selfdot.cobblemonmegas.common.command;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.selfdot.cobblemonmegas.common.CobblemonMegas;
import com.selfdot.cobblemonmegas.common.DataKeys;
import com.selfdot.cobblemonmegas.common.util.MegaUtils;
import com.selfdot.cobblemonmegas.common.util.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MegaEvolveSlotCommand implements Command<ServerCommandSource> {

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        Pokemon pokemon = PartySlotArgumentType.Companion.getPokemon(context, "pokemon");

        if (MegaUtils.containsMegaAspects(pokemon)) {
            MegaUtils.deMegaEvolve(pokemon);
            return SINGLE_SUCCESS;
        }

        String reasonCannotMegaEvolve = MegaUtils.reasonCannotMegaEvolve(player, pokemon);
        if (reasonCannotMegaEvolve != null) {
            context.getSource().sendError(Text.literal(reasonCannotMegaEvolve));
            return -1;
        }

        String megaType = DataKeys.MEGA;
        if (
            pokemon.getSpecies().getName().equalsIgnoreCase("charizard") ||
                pokemon.getSpecies().getName().equalsIgnoreCase("mewtwo")
        ) {
            NbtCompound nbt = NbtUtils.getNbt(pokemon.heldItem(), "");
            if (nbt.isEmpty() || !nbt.contains(DataKeys.NBT_KEY_MEGA_STONE)) return 0;

            String nbtString = nbt.getString(DataKeys.NBT_KEY_MEGA_STONE);
            if (nbtString.isEmpty()) return 0;
            megaType = nbt.getString(DataKeys.NBT_KEY_MEGA_STONE).endsWith("x") ?
                DataKeys.MEGA_X : DataKeys.MEGA_Y;
        }

        PokemonBattle battle = BattleRegistry.INSTANCE.getBattleByParticipatingPlayer(player);
        if (battle == null) {
            // Save the ability to restore it later
            MegaUtils.savePreviousAbility(pokemon);

            new FlagSpeciesFeature(megaType, true).apply(pokemon);
        } else {
            BattleActor battleActor = battle.getActor(player);
            if (battleActor == null) return 0;
            CobblemonMegas.getInstance().getToMegaEvolveThisTurn().add(battleActor.getUuid());
            context.getSource().sendMessage(Text.literal(
                pokemon.getDisplayName().getString() + " will mega evolve this turn if a move is used."
            ));
        }
        return SINGLE_SUCCESS;
    }

}