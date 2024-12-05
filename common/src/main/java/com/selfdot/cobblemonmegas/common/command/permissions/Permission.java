package com.selfdot.cobblemonmegas.common.command.permissions;

import com.selfdot.cobblemonmegas.common.DataKeys;
import net.minecraft.util.Identifier;

public record Permission(String literal, PermissionLevel level) {

    public Identifier getIdentifier() {
        return Identifier.of(DataKeys.MOD_NAMESPACE, literal);
    }

}
