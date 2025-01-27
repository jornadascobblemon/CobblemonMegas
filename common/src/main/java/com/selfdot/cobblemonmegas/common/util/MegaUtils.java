package com.selfdot.cobblemonmegas.common.util;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.abilities.*;
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

import java.util.*;
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
        BattlePokemon battlePokemon = activeBattlePokemon.getFirst().getBattlePokemon();
        if (battlePokemon == null) battlePokemon = playerBattleActor.getPokemonList().getFirst();

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

        Optional<Ability> previousAbility = getPreviousAbility(pokemon);
        // if we didn't find the previousAbility, better not to do anything
        if (previousAbility.isEmpty()) return;

        pokemon.updateAbility(previousAbility.get());

        if (!pokemon.getAbility().getName().equalsIgnoreCase(previousAbility.get().getName())) {
            // only remove the nbt if the update was successful
            NbtUtils.removePokemonNbtString(pokemon, DataKeys.NBT_KEY_PREVIOUS_ABILITY);
        }
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
        BattlePokemon battlePokemon = activeBattlePokemon.getFirst().getBattlePokemon();
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

    public static void savePreviousAbility(Pokemon pokemon) {
        // Save an NBT string in the Pokémon persistent data with the key "previousAbility" and the name of the ability before it megaevolves
        Ability previousAbility = pokemon.getAbility();
        NbtUtils.setPokemonNbtString(pokemon, DataKeys.NBT_KEY_PREVIOUS_ABILITY, previousAbility.getName());
    }

    public static boolean containsMegaAspects(Pokemon pokemon) {
        return Stream.of(DataKeys.MEGA, DataKeys.MEGA_X, DataKeys.MEGA_Y)
            .anyMatch(aspect -> pokemon.getAspects().contains(aspect));
    }

    private static void removeMegaAspects(Pokemon pokemon) {
        // For each of the mega aspects, check if it's in the features and remove it
        Stream.of(DataKeys.MEGA, DataKeys.MEGA_X, DataKeys.MEGA_Y)
            .filter(aspect -> pokemon.getAspects().contains(aspect))
            .forEach(aspect -> new FlagSpeciesFeature(aspect, false).apply(pokemon));
    }

    /**
     * Retrieves a Pokémon's previous ability from its persistent data, species ability pool or the global Abilities registries.
     * If the previous ability is not stored in the Pokémon's persistent data, the method falls back to the first ability found from the species AbilityPool.
     *
     * @param pokemon the Pokémon to retrieve the previous ability for
     * @return an Optional containing the previous ability, or an empty Optional if not found
     */
    private static Optional<Ability> getPreviousAbility(Pokemon pokemon) {
        NbtCompound pokemonNbt = pokemon.getPersistentData();
        AbilityPool speciesAbilities = pokemon.getSpecies().getAbilities();
        String previousAbilityName = pokemonNbt.getString(DataKeys.NBT_KEY_PREVIOUS_ABILITY);

        // If no previous ability is stored, fallback to the first ability from the species
        if (previousAbilityName.isEmpty()) {
            log.warn("Previous ability value is empty. Using the first species ability as a fallback for {}.",
                    pokemon.getDisplayName().getString());

            for (PotentialAbility pa : speciesAbilities) {
                // Return the first one we encounter
                return Optional.of(pa.getTemplate().create(false, pa.getPriority()));
            }

            // If speciesAbilities is empty for some reason
            log.error("No abilities found in species for Pokémon {}.",
                    pokemon.getDisplayName().getString());
            return Optional.empty();
        }

        // Attempt to find the previous ability in the species ability pool
        for (PotentialAbility pa : speciesAbilities) {
            AbilityTemplate template = pa.getTemplate();
            if (template.getName().equalsIgnoreCase(previousAbilityName)) {
                return Optional.of(template.create(false, pa.getPriority()));
            }
        }

        // if we didn't find the ability in the species AbilityPool, something is sus, but we're going to set it anyway
        // if the species have this ability, check if it was removed from the AbilityPool at runtime
        // if the species doesn't have the ability, check if this ability was added to this particular Pokémon at runtime
        // check other mods or data packs that can be messing with species and/or pokémon abilities
        AbilityTemplate previousAbilityTemplate = Abilities.INSTANCE.get(previousAbilityName.toLowerCase());
        if (previousAbilityTemplate != null) {
            ServerPlayerEntity ownerPlayer = pokemon.getOwnerPlayer();
            String ownerPlayerName = ownerPlayer == null ? "" : pokemon.getOwnerPlayer().getName().getString();
            String ownerPlayerUuid = ownerPlayer == null ? "" : pokemon.getOwnerPlayer().getUuid().toString();
            log.warn("Ability '{}' of Pokemon '{}' with UUID '{}' from owner '{}' with UUID '{}' was found in the Abilities registry, but not in the species '{}' AbilityPool. This is means that this ability will be set as forced and if the pokemons tries to change his ability in any other way, it will be ignored, including ability patches. Setting anyway...",
                    previousAbilityName,
                    pokemon.getDisplayName().getString(),
                    pokemon.getSpecies().getName(),
                    pokemon.getUuid(),
                    ownerPlayerName,
                    ownerPlayerUuid
            );

            // In this case, we are setting forced = true but even if we set forced = false, it will be ignored in Pokemon.attachAbilityCoordinate()
            return Optional.of(previousAbilityTemplate.create(true, Priority.NORMAL));
        }

        // At this point, we failed to find the ability anywhere
        log.error("Couldn't find previous ability '{}' for Pokémon {} in either species pool or in the Abilities registry.",
                previousAbilityName, pokemon.getDisplayName().getString());
        return Optional.empty();
    }
}
