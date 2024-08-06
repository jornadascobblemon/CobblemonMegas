package com.selfdot.cobblemonmegas.common.command.permissions;

import java.util.List;

import static com.selfdot.cobblemonmegas.common.command.permissions.PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS;

public class MegasPermissions {

    private static final String MEGAS = "megas.";
    public static final Permission RELOAD = new Permission(MEGAS + "reload", CHEAT_COMMANDS_AND_COMMAND_BLOCKS);
    public static final Permission MEGA_EVOLVE = new Permission(
        MEGAS + "megaevolve", CHEAT_COMMANDS_AND_COMMAND_BLOCKS
    );
    public static final Permission MEGA_EVOLVE_SLOT = new Permission(
        MEGAS + "megaevolveslot", CHEAT_COMMANDS_AND_COMMAND_BLOCKS
    );
    public static final Permission GET_MEGA_STONE = new Permission(
        MEGAS + "getmegastone", CHEAT_COMMANDS_AND_COMMAND_BLOCKS
    );
    public static final Permission GIVE_MEGA_STONE = new Permission(
        MEGAS + "givemegastone", CHEAT_COMMANDS_AND_COMMAND_BLOCKS
    );
    public static final Permission GIVE_KEY_STONE = new Permission(
        MEGAS + "givekeystone", CHEAT_COMMANDS_AND_COMMAND_BLOCKS
    );
    public static final Permission MIGRATE_STONE = new Permission(
            MEGAS + "migratestone", CHEAT_COMMANDS_AND_COMMAND_BLOCKS
    );

    public static List<Permission> all() {
        return List.of(RELOAD, MEGA_EVOLVE, MEGA_EVOLVE_SLOT, GET_MEGA_STONE, GIVE_MEGA_STONE, GIVE_KEY_STONE, MIGRATE_STONE);
    }

}
