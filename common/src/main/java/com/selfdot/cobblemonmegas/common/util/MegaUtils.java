package com.selfdot.cobblemonmegas.common.util;

import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.AbilityPool;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static net.minecraft.component.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE;

@Slf4j
public class MegaUtils {

    public static String reasonCannotMegaEvolve(ServerPlayerEntity player, Pokemon pokemon) {
        if (
            Stream.of(DataKeys.MEGA, DataKeys.MEGA_X, DataKeys.MEGA_Y)
                .noneMatch(aspect -> pokemon.getSpecies().getFeatures().contains(aspect))
        ) {
            return "This species cannot mega evolve.";
        }

        if (!MegaStoneHeldItemManager.getInstance().isHoldingValidMegaStone(pokemon)) {
            return "This Pokémon is not holding their Mega Stone.";
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
                actor.getPokemonList().forEach(battlePokemon -> {
                    Pokemon pokemon = battlePokemon.getOriginalPokemon();
                    if (containsMegaAspects(pokemon)) deMegaEvolve(pokemon);
                });
            }
        );
    }

    public static void deMegaEvolve(Pokemon pokemon) {
        // Remove the mega aspect
        removeMegaAspects(pokemon);

        // Restore the original ability
        NbtCompound pokemonNbt = pokemon.getPersistentData();
        if (pokemonNbt.isEmpty() || !pokemonNbt.contains(DataKeys.NBT_KEY_PREVIOUS_ABILITY)) return;
        restorePreviousAbility(pokemon, pokemonNbt);
    }

    private static void sendError(ServerPlayerEntity player, String error) {
        player.sendMessage(Text.literal(Formatting.RED + error));
    }

    public static void updateKeyStoneGlow(ItemStack itemStack, PlayerEntity player) {
        NbtCompound nbt = NbtUtils.getNbt(itemStack, DataKeys.MOD_NAMESPACE);
        if (nbt.isEmpty() || !nbt.contains(DataKeys.NBT_KEY_KEY_STONE)) return;

        if (CobblemonMegas.getInstance().getToMegaEvolveThisTurn().contains(player.getUuid())) {
            itemStack.set(ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            itemStack.set(ENCHANTMENT_GLINT_OVERRIDE, false);
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

        // Save the ability to restore it later
        savePreviousAbility(pokemon);
        return true;
    }

    // Save an NBT string in the Pokémon persistent data with the key "previousAbility" and the name of the ability before it megaevolves
    public static void savePreviousAbility(Pokemon pokemon) {
        Ability previousAbility = pokemon.getAbility();
        pokemon.getPersistentData().putString(DataKeys.NBT_KEY_PREVIOUS_ABILITY, previousAbility.getName());
        // we have to emit to update the persistent data
        pokemon.getAnyChangeObservable().emit();
    }

    public static boolean containsMegaAspects(Pokemon pokemon) {
        return Stream.of(DataKeys.MEGA, DataKeys.MEGA_X, DataKeys.MEGA_Y)
            .anyMatch(aspect -> pokemon.getAspects().contains(aspect));
    }

    private static void removeMegaAspects(Pokemon pokemon) {
        // For each of the mega aspects, check if it's in the features and remove it
        Stream.of(DataKeys.MEGA, DataKeys.MEGA_X, DataKeys.MEGA_Y)
            .filter(aspect -> pokemon.getAspects().contains(aspect))
            .forEach(aspect -> {
                new FlagSpeciesFeature(aspect, false).apply(pokemon);
            });
    }

    private static void restorePreviousAbility(Pokemon pokemon, NbtCompound pokemonNbt) {
        String previousAbilityName = pokemonNbt.getString(DataKeys.NBT_KEY_PREVIOUS_ABILITY);
        AbilityPool speciesAbilities = pokemon.getSpecies().getAbilities();

        if (previousAbilityName.isEmpty()) {
            log.error("Pokémon previous ability value is empty. Choosing the first ability from the species ability pool as a fallback.");
            previousAbilityName = speciesAbilities.iterator().next().getTemplate().getName();
        }

        // check if the species AbilityPool contains the ability named `previousAbilityName`
        for (PotentialAbility ability : speciesAbilities) {
            if (ability.getTemplate().getName().equalsIgnoreCase(previousAbilityName)) {
                pokemon.updateAbility(ability.getTemplate().create(false, ability.getPriority()));
                break; // if matched, don't need to check other abilities
            }
        }
    }
}
