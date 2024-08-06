package com.selfdot.cobblemonmegas.common.command;

import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.selfdot.cobblemonmegas.common.CobblemonMegas;
import com.selfdot.cobblemonmegas.common.item.MegaStoneHeldItemManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.selfdot.cobblemonmegas.common.command.permissions.MegasPermissions.*;

public class CommandTree {

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess commandRegistryAccess,
            CommandManager.RegistrationEnvironment registrationEnvironment
    ) {
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>
                        literal("megas")
                .requires(source -> CobblemonMegas.getInstance().getPermissionValidator().hasPermission(source, RELOAD))
                .then(LiteralArgumentBuilder.<ServerCommandSource>
                                literal("reload")
                        .executes(new ReloadCommand())
                )
        );
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>
                        literal("megaevolve")
                .requires(source ->
                        !CobblemonMegas.getInstance().isDisabled() &&
                                source.isExecutedByPlayer() &&
                                CobblemonMegas.getInstance().getPermissionValidator().hasPermission(source, MEGA_EVOLVE)
                )
                .executes(new MegaEvolveInBattleCommand())
        );
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>
                        literal("megaevolve")
                .requires(source ->
                        !CobblemonMegas.getInstance().isDisabled() &&
                                source.isExecutedByPlayer() &&
                                CobblemonMegas.getInstance().getPermissionValidator().hasPermission(source, MEGA_EVOLVE_SLOT)
                )
                .then(RequiredArgumentBuilder.<ServerCommandSource, Integer>
                                argument("pokemon", PartySlotArgumentType.Companion.partySlot())
                        .executes(new MegaEvolveSlotCommand())
                )
        );
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>
                        literal("getmegastone")
                .requires(source ->
                        !CobblemonMegas.getInstance().isDisabled() &&
                                source.isExecutedByPlayer() &&
                                CobblemonMegas.getInstance().getPermissionValidator().hasPermission(source, GET_MEGA_STONE)
                )
                .then(RequiredArgumentBuilder.<ServerCommandSource, String>
                                argument("megaStone", string())
                        .suggests(CommandTree::megaStoneIDSuggestions)
                        .executes(new GetMegaStoneCommand())
                )
        );
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>
                        literal("givemegastone")
                .requires(source ->
                        !CobblemonMegas.getInstance().isDisabled() &&
                                CobblemonMegas.getInstance().getPermissionValidator().hasPermission(source, GIVE_MEGA_STONE)
                )
                .then(RequiredArgumentBuilder.<ServerCommandSource, String>
                                argument("megaStone", string())
                        .suggests(CommandTree::megaStoneIDSuggestions)
                        .then(RequiredArgumentBuilder.<ServerCommandSource, EntitySelector>
                                        argument("players", EntityArgumentType.players())
                                .executes(new GiveMegaStoneCommand())
                        )
                )
        );
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>
                        literal("givekeystone")
                .requires(source ->
                        !CobblemonMegas.getInstance().isDisabled() &&
                                CobblemonMegas.getInstance().getPermissionValidator().hasPermission(source, GIVE_KEY_STONE)
                )
                .then(RequiredArgumentBuilder.<ServerCommandSource, String>
                                argument("keyStone", string())
                        .suggests((context, builder) -> {
                            GiveKeyStoneCommand.KEY_STONE_TYPES.keySet().forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .then(RequiredArgumentBuilder.<ServerCommandSource, EntitySelector>
                                        argument("players", EntityArgumentType.players())
                                .executes(new GiveKeyStoneCommand())
                        )
                )
        );
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>
                        literal("migratestone")
                .requires(source ->
                        !CobblemonMegas.getInstance().isDisabled() &&
                                CobblemonMegas.getInstance().getPermissionValidator().hasPermission(source, MIGRATE_STONE))
                .executes(new MigrateMegaStone()));
    }

    private static CompletableFuture<Suggestions> megaStoneIDSuggestions(
            CommandContext<ServerCommandSource> context,
            SuggestionsBuilder builder
    ) {
        MegaStoneHeldItemManager.getInstance().getAllMegaStoneIds().stream()
                .filter(id ->
                        CobblemonMegas.getInstance().getConfig().getMegaStoneWhitelist().contains(id)
                )
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

}
