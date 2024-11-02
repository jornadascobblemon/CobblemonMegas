package com.selfdot.cobblemonmegas.common.util;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.selfdot.cobblemonmegas.common.CobblemonMegas;
import com.selfdot.cobblemonmegas.common.DataKeys;
import com.selfdot.cobblemonmegas.common.item.MegaStoneHeldItemManager;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class MegaUtils {

    public static String reasonCannotMegaEvolve(ServerPlayerEntity player, Pokemon pokemon) {
        if (
            Stream.of(DataKeys.MEGA, DataKeys.MEGA_X, DataKeys.MEGA_Y, DataKeys.PRIMAL)
                .noneMatch(aspect -> pokemon.getSpecies().getFeatures().contains(aspect))
        ) {
            return "“This species cannot megaevolve and has no primal form.”";
        }

        if (!MegaStoneHeldItemManager.getInstance().isHoldingValidMegaStone(pokemon)) {
            return "This Pokémon is not holding its Mega Stone or orb.";
        }

        if (pokemon.getSpecies().getName().equalsIgnoreCase("rayquaza")) {
            if (!CobblemonMegas.getInstance().getConfig().isMegaRayquazaAllowed()) {
                return "Mega Rayquaza is not allowed on this server.";
            } else if (
                pokemon.getMoveSet().getMoves().stream()
                    .noneMatch(move -> move.getName().equalsIgnoreCase("dragonascent"))
            ) {
                return "Rayquaza must know Dragon Ascent to mega evolve.";
            }
        } else if (
            !CobblemonMegas.getInstance().getConfig().getMegaStoneWhitelist().contains(
                MegaStoneHeldItemManager.getInstance().showdownId(pokemon)
            )
        ) {
            return "This mega stone cannot be used on this server.";
        }

        PokemonBattle battle = BattleRegistry.INSTANCE.getBattleByParticipatingPlayer(player);
        if (battle == null) return null;

        if (CobblemonMegas.getInstance().getHasMegaEvolvedThisBattle().contains(player.getUuid())) {
            return "Mega evolution can only be used once per battle.";
        }

        BattleActor playerBattleActor = battle.getActor(player);
        if (playerBattleActor == null) return "PlayerBattleActor is null (Report this error)";
        List<ActiveBattlePokemon> activeBattlePokemon = playerBattleActor.getActivePokemon();
        if (activeBattlePokemon.size() != 1) return "Mega evolution is currently only available in 1v1 battles.";
        BattlePokemon battlePokemon = activeBattlePokemon.get(0).getBattlePokemon();
        if (battlePokemon == null) battlePokemon = playerBattleActor.getPokemonList().get(0);

        if (!battlePokemon.getEffectedPokemon().getUuid().equals(pokemon.getUuid())) {
            return "This is not your active battle Pokémon.";
        }

        return null;
    }

    public static void deMegaEvolveAllPlayers(PokemonBattle battle) {
        battle.getActors().forEach(
            actor -> {
                if (!actor.getPlayerUUIDs().iterator().hasNext()) return;
                actor.getPokemonList().forEach(battlePokemon -> deMegaEvolve(battlePokemon.getOriginalPokemon()));
            }
        );
    }

    public static void deMegaEvolve(Pokemon pokemon) {
        Stream.of(DataKeys.MEGA, DataKeys.MEGA_X, DataKeys.MEGA_Y, DataKeys.PRIMAL)
            .forEach(megaAspect -> new FlagSpeciesFeature(megaAspect, false).apply(pokemon));
    }

    private static void sendError(ServerPlayerEntity player, String error) {
        player.sendMessage(Text.literal(Formatting.RED + error));
    }

    public static void updateKeyStoneGlow(ItemStack itemStack, PlayerEntity player) {
        NbtCompound nbt = itemStack.getNbt();
        if (nbt == null || !nbt.getBoolean(DataKeys.NBT_KEY_KEY_STONE)) return;
        if (CobblemonMegas.getInstance().getToMegaEvolveThisTurn().contains(player.getUuid())) {
            NbtList enchantmentsNbt = new NbtList();
            enchantmentsNbt.add(new NbtCompound());
            nbt.put("Enchantments", enchantmentsNbt);
        } else {
            nbt.remove("Enchantments");
        }
    }

    public static void updateKeyStoneGlow(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            updateKeyStoneGlow(player.getInventory().getStack(i), player);
        }
    }

    public static boolean attemptMegaEvolveInBattle(ServerPlayerEntity player, boolean shouldTellSuccess) {
        PokemonBattle battle = BattleRegistry.INSTANCE.getBattleByParticipatingPlayer(player);
        if (battle == null) {
            sendError(player, "This can only be used in battle.");
            return false;
        }

        BattleActor playerBattleActor = battle.getActor(player);
        if (playerBattleActor == null) return false;
        List<ActiveBattlePokemon> activeBattlePokemon = playerBattleActor.getActivePokemon();
        if (activeBattlePokemon.size() != 1) return false;
        BattlePokemon battlePokemon = activeBattlePokemon.get(0).getBattlePokemon();
        if (battlePokemon == null) return false;
        Pokemon pokemon = battlePokemon.getEffectedPokemon();

        Set<UUID> toMegaEvolveThisTurn = CobblemonMegas.getInstance().getToMegaEvolveThisTurn();
        UUID actorId = playerBattleActor.getUuid();
        if (toMegaEvolveThisTurn.contains(actorId)) {
            toMegaEvolveThisTurn.remove(actorId);
            updateKeyStoneGlow(player);
            if (shouldTellSuccess) {
                player.sendMessage(Text.literal(
                    pokemon.getDisplayName().getString() + " will no longer mega evolve this turn."
                ));
            }
            return true;
        }

        String reasonCannotMegaEvolve = MegaUtils.reasonCannotMegaEvolve(player, pokemon);
        if (reasonCannotMegaEvolve != null) {
            sendError(player, reasonCannotMegaEvolve);
            return false;
        }

        toMegaEvolveThisTurn.add(actorId);
        updateKeyStoneGlow(player);
        if (shouldTellSuccess) {
            player.sendMessage(Text.literal(
                pokemon.getDisplayName().getString() + " will mega evolve this turn if a move is used."
            ));
        }
        return true;
    }

}
