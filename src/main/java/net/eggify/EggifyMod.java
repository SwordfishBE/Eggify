package net.eggify;

import net.eggify.command.EggifyCommand;
import net.eggify.config.ConfigManager;
import net.eggify.config.EggifyConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EggifyMod implements ModInitializer {
    public static final String MOD_ID = "eggify";
    public static final String MINECRAFT_VERSION = "26.1.1";
    public static final String LOG_PREFIX = "[Eggify]";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ConfigManager CONFIG = new ConfigManager();

    @Override
    public void onInitialize() {
        EggifyConfig config = CONFIG.load();
        LOGGER.info("{} Mod initialized. useLuckPerms={}", LOG_PREFIX, config.useLuckPerms);
        EggifyCommand.register();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            boolean luckPermsInstalled = PermissionHelper.isLuckPermsInstalled();
            if (config.useLuckPerms && !luckPermsInstalled) {
                LOGGER.warn("{} LuckPerms support is enabled in config, but LuckPerms is not installed.", LOG_PREFIX);
            } else if (config.useLuckPerms) {
                LOGGER.info("{} LuckPerms support enabled and detected.", LOG_PREFIX);
            }

            UpdateChecker.checkForUpdates();
            LOGGER.info("{} Ready!", LOG_PREFIX);
        });
    }
}
