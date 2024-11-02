package com.selfdot.cobblemonmegas.common.mixin;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.battles.interpreter.instructions.DetailsChangeInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.selfdot.cobblemonmegas.common.CobblemonMegas;
import com.selfdot.cobblemonmegas.common.DataKeys;
import com.selfdot.cobblemonmegas.common.util.MegaUtils;
import kotlin.Unit;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DetailsChangeInstruction.class)
public abstract class DetailsChangeInstructionMixin {

    @Shadow(remap = false)
    public abstract BattleMessage getMessage();

    @Inject(method = "invoke", at = @At("TAIL"), remap = false)
    private void injectDetailsChangeChangeInstruction(PokemonBattle battle, CallbackInfo ci) {
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
                new FlagSpeciesFeature(megaType, true).apply(battlePokemon.getOriginalPokemon());
                new FlagSpeciesFeature(megaType, true).apply(battlePokemon.getEffectedPokemon());
                ServerPlayerEntity player = battlePokemon.getOriginalPokemon().getOwnerPlayer();
                if (player == null) return Unit.INSTANCE;
                CobblemonMegas.getInstance().getHasMegaEvolvedThisBattle().add(player.getUuid());
                MegaUtils.updateKeyStoneGlow(player);
                return Unit.INSTANCE;
            });
        }
    }

}
