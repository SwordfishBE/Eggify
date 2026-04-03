package com.eggify;

import com.eggify.config.EggifyConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;

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

        return hasLuckPermsPermission(player, USE_PERMISSION);
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

        return hasLuckPermsPermission(player, COMMAND_PERMISSION);
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

        return hasLuckPermsPermission(player, DEBUG_PERMISSION);
    }

    private static boolean isOpCommandSource(CommandSourceStack source, ServerPlayer player) {
        return source.getServer().getProfilePermissions(player.nameAndId())
            .hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
    }

    private static boolean hasLuckPermsPermission(ServerPlayer player, String permission) {
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUUID());
            if (user == null) {
                EggifyMod.LOGGER.debug("{} LuckPerms user not loaded for {}", EggifyMod.LOG_PREFIX, player.getScoreboardName());
                return false;
            }

            QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(player);
            return user.getCachedData().getPermissionData(queryOptions).checkPermission(permission).asBoolean();
        } catch (IllegalStateException exception) {
            EggifyMod.LOGGER.debug("{} LuckPerms API is not ready: {}", EggifyMod.LOG_PREFIX, exception.getMessage());
            return false;
        }
    }
}
