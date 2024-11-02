package com.selfdot.cobblemonmegas.common.item;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.helditem.HeldItemManager;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.selfdot.cobblemonmegas.common.CobblemonMegas;
import com.selfdot.cobblemonmegas.common.DataKeys;
import com.selfdot.cobblemonmegas.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MegaStoneHeldItemManager implements HeldItemManager {

    private static final MegaStoneHeldItemManager INSTANCE = new MegaStoneHeldItemManager();
    private MegaStoneHeldItemManager() { }
    public static MegaStoneHeldItemManager getInstance() {
        return INSTANCE;
    }

    public ItemStack getMegaStoneItem(String id) {
        if (!MEGA_STONE_IDS.containsKey(id)) return ItemStack.EMPTY;
        if (!CobblemonMegas.getInstance().getConfig().getMegaStoneWhitelist().contains(id)) {
            return ItemStack.EMPTY;
        }
        ItemStack megaStone = new ItemStack(Items.EMERALD);
        NbtCompound nbt = megaStone.getOrCreateNbt();
        nbt.putString(DataKeys.NBT_KEY_MEGA_STONE, id);
        nbt.putInt("CustomModelData", customModelData(id));
        nbt.putBoolean("italic", false);
        String displayName = id.substring(0, 1).toUpperCase() + id.substring(1);
        if (displayName.endsWith("x") || displayName.endsWith("y")) {
            displayName = displayName.substring(0, id.length() - 1) +
                    displayName.substring(id.length() - 1).toUpperCase();
        }
        ItemUtils.setNameNoItalics(megaStone, displayName);
        return megaStone;
    }

    @Override
    public void give(@NotNull BattlePokemon battlePokemon, @NotNull String s) {
        battlePokemon.getEffectedPokemon().swapHeldItem(getMegaStoneItem(s), false);
    }

    @Override
    public void handleEndInstruction(
        @NotNull BattlePokemon battlePokemon,
        @NotNull PokemonBattle pokemonBattle,
        @NotNull BattleMessage battleMessage
    ) { }

    @Override
    public void handleStartInstruction(
        @NotNull BattlePokemon battlePokemon,
        @NotNull PokemonBattle pokemonBattle,
        @NotNull BattleMessage battleMessage
    ) { }

    @NotNull
    @Override
    public Text nameOf(@NotNull String s) {
        return Text.of(s);
    }

    @Override
    public boolean shouldConsumeItem(
        @NotNull BattlePokemon battlePokemon,
        @NotNull PokemonBattle pokemonBattle,
        @NotNull String s
    ) {
        return false;
    }

    public String showdownId(Pokemon pokemon) {
        ItemStack itemStack = pokemon.heldItem();
        NbtCompound nbt = itemStack.getNbt();
        if (nbt == null) return null;
        String id = nbt.getString(DataKeys.NBT_KEY_MEGA_STONE);
        if (!MEGA_STONE_IDS.containsKey(id)) return null;
        return id;
    }

    @Nullable
    @Override
    public String showdownId(@NotNull BattlePokemon battlePokemon) {
        return showdownId(battlePokemon.getEffectedPokemon());
    }

    @Override
    public void take(@NotNull BattlePokemon battlePokemon, @NotNull String s) {
        battlePokemon.getEffectedPokemon().removeHeldItem();
    }

    private final Map<String, Species> MEGA_STONE_IDS = new HashMap<>();
    private Species getSpecies(String name) {
        return PokemonSpecies.INSTANCE.getByName(name);
    }

    public boolean isHoldingValidMegaStone(Pokemon pokemon) {
        Species species = pokemon.getSpecies();
        if (species.getName().equalsIgnoreCase("rayquaza")) return true;

        String showdownId = showdownId(pokemon);
        if (showdownId == null) return false;
        return MEGA_STONE_IDS.get(showdownId).equals(species);
    }

    public Set<String> getAllMegaStoneIds() {
        return MEGA_STONE_IDS.keySet();
    }

    private static final int CUSTOM_MODEL_DATA_OFFSET = 100;
    private int customModelData(String showdownId) {
        if (!MEGA_STONE_IDS.containsKey(showdownId)) return 2;
        return CUSTOM_MODEL_DATA_OFFSET + MEGA_STONE_IDS.keySet().stream().sorted().toList().indexOf(showdownId);
    }

    public void loadMegaStoneIds() {
        MEGA_STONE_IDS.put("venusaurite", getSpecies("venusaur"));
        MEGA_STONE_IDS.put("charizardite-x", getSpecies("charizard"));
        MEGA_STONE_IDS.put("charizardite-y", getSpecies("charizard"));
        MEGA_STONE_IDS.put("blastoisinite", getSpecies("blastoise"));
        MEGA_STONE_IDS.put("alakazite", getSpecies("alakazam"));
        MEGA_STONE_IDS.put("gengarite", getSpecies("gengar"));
        MEGA_STONE_IDS.put("kangaskhanite", getSpecies("kangaskhan"));
        MEGA_STONE_IDS.put("pinsirite", getSpecies("pinsir"));
        MEGA_STONE_IDS.put("gyaradosite", getSpecies("gyarados"));
        MEGA_STONE_IDS.put("aerodactylite", getSpecies("aerodactyl"));
        MEGA_STONE_IDS.put("mewtwonite-x", getSpecies("mewtwo"));
        MEGA_STONE_IDS.put("mewtwonite-y", getSpecies("mewtwo"));
        MEGA_STONE_IDS.put("ampharosite", getSpecies("ampharos"));
        MEGA_STONE_IDS.put("scizorite", getSpecies("scizor"));
        MEGA_STONE_IDS.put("heracronite", getSpecies("heracross"));
        MEGA_STONE_IDS.put("houndoominite", getSpecies("houndoom"));
        MEGA_STONE_IDS.put("tyranitarite", getSpecies("tyranitar"));
        MEGA_STONE_IDS.put("blazikenite", getSpecies("blaziken"));
        MEGA_STONE_IDS.put("gardevoirite", getSpecies("gardevoir"));
        MEGA_STONE_IDS.put("mawilite", getSpecies("mawile"));
        MEGA_STONE_IDS.put("aggronite", getSpecies("aggron"));
        MEGA_STONE_IDS.put("medichamite", getSpecies("medicham"));
        MEGA_STONE_IDS.put("manectite", getSpecies("manetric"));
        MEGA_STONE_IDS.put("banettite", getSpecies("banette"));
        MEGA_STONE_IDS.put("absolite", getSpecies("absol"));
        MEGA_STONE_IDS.put("latiasite", getSpecies("latias"));
        MEGA_STONE_IDS.put("latiosite", getSpecies("latios"));
        MEGA_STONE_IDS.put("garchompite", getSpecies("garchomp"));
        MEGA_STONE_IDS.put("lucarionite", getSpecies("lucario"));
        MEGA_STONE_IDS.put("abomasite", getSpecies("abomasnow"));
        MEGA_STONE_IDS.put("beedrillite", getSpecies("beedrill"));
        MEGA_STONE_IDS.put("pidgeotite", getSpecies("pidgeot"));
        MEGA_STONE_IDS.put("slowbronite", getSpecies("slowbro"));
        MEGA_STONE_IDS.put("steelixite", getSpecies("steelix"));
        MEGA_STONE_IDS.put("sceptilite", getSpecies("sceptile"));
        MEGA_STONE_IDS.put("swampertite", getSpecies("swampert"));
        MEGA_STONE_IDS.put("sablenite", getSpecies("sableye"));
        MEGA_STONE_IDS.put("sharpedonite", getSpecies("sharpedo"));
        MEGA_STONE_IDS.put("cameruptite", getSpecies("camerupt"));
        MEGA_STONE_IDS.put("altarianite", getSpecies("altaria"));
        MEGA_STONE_IDS.put("glalitite", getSpecies("glalie"));
        MEGA_STONE_IDS.put("salamencite", getSpecies("salamence"));
        MEGA_STONE_IDS.put("metagrossite", getSpecies("metagross"));
        MEGA_STONE_IDS.put("lopunnite", getSpecies("lopunny"));
        MEGA_STONE_IDS.put("galladite", getSpecies("gallade"));
        MEGA_STONE_IDS.put("audinite", getSpecies("audino"));
        MEGA_STONE_IDS.put("diancite", getSpecies("diancie"));
        MEGA_STONE_IDS.put("blueorb", getSpecies("kyogre"));
        MEGA_STONE_IDS.put("redorb", getSpecies("groudon"));
    }
}


