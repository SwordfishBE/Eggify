package net.eggify;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.eggify.config.EggifyConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

public final class PermissionHelper {
    public static final String USE_PERMISSION = "eggify.use";
    public static final String COMMAND_PERMISSION = "eggify.command";
    public static final String DEBUG_PERMISSION = "eggify.debug";
    public static final String SPECIAL_EGG_CRAFT_PERMISSION = "eggify.special.craft";
    public static final String SPECIAL_EGG_USE_PERMISSION = "eggify.special.use";

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

        return hasPermission(player, USE_PERMISSION);
    }

    public static boolean canUseCommand(CommandSourceStack source) {
        EggifyConfig config = EggifyMod.CONFIG.getConfig();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return true;
        }

        if (isOpCommandSource(source, player)) {
            return true;
        }

        if (usesLuckPermsPermissions(config)) {
            return hasPermission(player, COMMAND_PERMISSION);
        }

        if (!config.allowCommandPermissionNode) {
            return false;
        }
        return true;
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

        if (usesLuckPermsPermissions(config)) {
            return hasPermission(player, DEBUG_PERMISSION);
        }

        return config.allowDebugCommand;
    }

    public static boolean canCraftSpecialEgg(Player player) {
        EggifyConfig config = EggifyMod.CONFIG.getConfig();
        if (!config.enableSpecialEgg) {
            return false;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }

        if (!usesLuckPermsPermissions(config)) {
            return true;
        }

        return hasPermission(serverPlayer, SPECIAL_EGG_CRAFT_PERMISSION);
    }

    public static boolean canUseSpecialEgg(ServerPlayer player) {
        EggifyConfig config = EggifyMod.CONFIG.getConfig();
        if (!config.enableSpecialEgg) {
            return false;
        }

        if (!canEggify(player)) {
            return false;
        }

        if (!usesLuckPermsPermissions(config)) {
            return true;
        }

        return hasPermission(player, SPECIAL_EGG_USE_PERMISSION);
    }

    public static boolean usesLuckPermsPermissions(EggifyConfig config) {
        return config.useLuckPerms && isLuckPermsInstalled();
    }

    private static boolean isOpCommandSource(CommandSourceStack source, ServerPlayer player) {
        return source.getServer().getProfilePermissions(player.nameAndId())
            .hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
    }

    private static boolean hasPermission(ServerPlayer player, String permission) {
        return Permissions.check(player, permission, false);
    }
}
