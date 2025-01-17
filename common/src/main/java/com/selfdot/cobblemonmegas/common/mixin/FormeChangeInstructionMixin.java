package com.selfdot.cobblemonmegas.common.mixin;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.battles.interpreter.instructions.FormeChangeInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.*;
import com.selfdot.cobblemonmegas.common.CobblemonMegas;
import com.selfdot.cobblemonmegas.common.DataKeys;
import com.selfdot.cobblemonmegas.common.util.MegaUtils;
import kotlin.Unit;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FormeChangeInstruction.class)
public abstract class FormeChangeInstructionMixin {

    @Shadow(remap = false)
    public abstract BattleMessage getMessage();

    @Inject(method = "invoke", at = @At("TAIL"), remap = false)
    private void injectFormeChangeChangeInstruction(PokemonBattle battle, CallbackInfo ci) {
        // example rawMessage:
        // "detailschange|p1a: 01942d82-d8f2-7fc3-acf5-f930233dd5cb|Heracross-Mega, 01942d82-d8f2-7fc3-acf5-f930233dd5cb, M"
        String s1 = getMessage().argumentAt(1);
        if (s1 == null) return;
        String[] s2 = s1.split(",");
        if (s2.length == 0) return;
        String[] s3 = s2[0].split("-");
        if (s3.length < 2) return;
        if (s3[1].equalsIgnoreCase(DataKeys.MEGA)) {
            BattlePokemon battlePokemon = getMessage().battlePokemon(0, battle);
            if (battlePokemon == null) return;
            String megaStone = battlePokemon.getHeldItemManager().showdownId(battlePokemon);
            if (megaStone == null) return;
            battle.dispatchGo(() -> {
                String megaType = DataKeys.MEGA;
                if      (megaStone.endsWith("x")) megaType = DataKeys.MEGA_X;
                else if (megaStone.endsWith("y")) megaType = DataKeys.MEGA_Y;
                Pokemon originalPokemon = battlePokemon.getOriginalPokemon();
                Pokemon effectedPokemon = battlePokemon.getEffectedPokemon();

                // Save the ability to restore it later
                MegaUtils.savePreviousAbility(originalPokemon);
                MegaUtils.savePreviousAbility(effectedPokemon);

                // TODO: transform originalPokemon and effectPokemon to JSON and print to console
//                System.out.println("original pokemon before setting flagspeciesfeature: " + originalPokemon.saveToJSON(DynamicRegistryManager.EMPTY, new JsonObject()));
//                System.out.println("effectedPokemonJson before setting flagspeciesfeature: " + effectedPokemon.saveToJSON(DynamicRegistryManager.EMPTY, new JsonObject()));

                new FlagSpeciesFeature(megaType, true).apply(originalPokemon);
                new FlagSpeciesFeature(megaType, true).apply(effectedPokemon);

//                System.out.println("original pokemon after setting flagspeciesfeature: " + originalPokemon.saveToJSON(DynamicRegistryManager.EMPTY, new JsonObject()));
//                System.out.println("effectedPokemonJson after setting flagspeciesfeature: " + effectedPokemon.saveToJSON(DynamicRegistryManager.EMPTY, new JsonObject()));


                ServerPlayerEntity player = battlePokemon.getOriginalPokemon().getOwnerPlayer();
                if (player == null) return Unit.INSTANCE;
                CobblemonMegas.getInstance().getHasMegaEvolvedThisBattle().add(player.getUuid());
                MegaUtils.updateKeyStoneGlow(player);
                return Unit.INSTANCE;
            });
        }
    }
}