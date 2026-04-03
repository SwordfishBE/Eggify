package com.eggify;

import com.eggify.config.EggifyConfig;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public final class PermissionHelper {
    public static final String USE_PERMISSION = "eggify.use";
    public static final String COMMAND_PERMISSION = "eggify.command";
    public static final String DEBUG_PERMISSION = "eggify.debug";

    private PermissionHelper() {
    }

    public static boolean isLuckPermsInstalled() {
        return FabricLoader.getInstance().isModLoaded("luckperms");
    }

    public static boolean canEggify(ServerPlayer player) {
        EggifyConfig config = EggifyMod.CONFIG.getConfig();
        if (!config.useLuckPerms || !isLuckPermsInstalled()) {
            return true;
        }

        return Permissions.check(player, USE_PERMISSION, false);
    }

    public static boolean canUseCommand(CommandSourceStack source) {
        EggifyConfig config = EggifyMod.CONFIG.getConfig();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return true;
        }

        if (isOpCommandSource(source, player)) {
            return true;
        }

        if (!config.allowCommandPermissionNode || !config.useLuckPerms || !isLuckPermsInstalled()) {
            return false;
        }

        return Permissions.check(source, COMMAND_PERMISSION, false);
    }

    public static boolean canReloadCommand(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return true;
        }

        return isOpCommandSource(source, player);
    }

    public static boolean canDebugCommand(CommandSourceStack source) {
        EggifyConfig config = EggifyMod.CONFIG.getConfig();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return true;
        }

        if (isOpCommandSource(source, player)) {
            return true;
        }

        if (!config.useLuckPerms || !isLuckPermsInstalled()) {
            return false;
        }

        return Permissions.check(source, DEBUG_PERMISSION, false);
    }

    private static boolean isOpCommandSource(CommandSourceStack source, ServerPlayer player) {
        return source.getServer().getProfilePermissions(player.nameAndId())
            .hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
    }
}
