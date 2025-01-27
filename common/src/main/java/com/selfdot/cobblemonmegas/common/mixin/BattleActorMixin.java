package com.selfdot.cobblemonmegas.common.mixin;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.MoveActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.selfdot.cobblemonmegas.common.CobblemonMegas;
import com.selfdot.cobblemonmegas.common.util.MegaUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(BattleActor.class)
public abstract class BattleActorMixin {

    @Shadow(remap = false) public abstract UUID getUuid();
    @Shadow(remap = false) public abstract Iterable<UUID> getPlayerUUIDs();

    @Shadow(remap = false) private List<ShowdownActionResponse> responses;

    @Shadow(remap = false) @Final private List<ActiveBattlePokemon> activePokemon;

    @Inject(method = "writeShowdownResponse", at = @At("HEAD"), remap = false)
    private void injectWriteShowdownResponse(CallbackInfo ci) {
        UUID uuid = getUuid();
        if (CobblemonMegas.getInstance().getToMegaEvolveThisTurn().remove(uuid)) {
            if (responses.size() != 1) return;
            if (!(responses.getFirst() instanceof MoveActionResponse moveActionResponse)) return;
            if (activePokemon.size() != 1) return;
            BattlePokemon battlePokemon = activePokemon.getFirst().getBattlePokemon();
            if (battlePokemon == null) return;
            String megaStone = battlePokemon.getHeldItemManager().showdownId(battlePokemon);
            if (megaStone == null) return;
            moveActionResponse.setGimmickID(ShowdownMoveset.Gimmick.MEGA_EVOLUTION.getId());
        }
    }

    @Inject(method = "turn", at = @At("HEAD"), remap = false)
    private void injectTurn(CallbackInfo ci) {
        getPlayerUUIDs().forEach(playerId -> {
            ServerPlayerEntity player = CobblemonMegas.getInstance()
                .getServer().getPlayerManager().getPlayer(playerId);
            if (player == null) return;
            MegaUtils.updateKeyStoneGlow(player);
        });
    }

}
