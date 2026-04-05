package net.eggify;

import net.eggify.command.EggifyCommand;
import net.eggify.config.ConfigManager;
import net.eggify.config.EggifyConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EggifyMod implements ModInitializer {
    public static final String MOD_ID = "eggify";
    private static final ModContainer MOD_CONTAINER = FabricLoader.getInstance()
        .getModContainer(MOD_ID)
        .orElseThrow(() -> new IllegalStateException("Missing mod metadata for " + MOD_ID));
    public static final String MOD_NAME = MOD_CONTAINER.getMetadata().getName();
    public static final String MOD_VERSION = MOD_CONTAINER.getMetadata().getVersion().getFriendlyString();
    public static final String LOG_PREFIX = "[" + MOD_NAME + "]";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ConfigManager CONFIG = new ConfigManager();

    @Override
    public void onInitialize() {
        EggifyConfig config = CONFIG.load();
        LOGGER.info("{} Mod initialized. Version: {}", LOG_PREFIX, MOD_VERSION);
        EggifyCommand.register();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            boolean luckPermsInstalled = PermissionHelper.isLuckPermsInstalled();
            if (config.useLuckPerms && !luckPermsInstalled) {
                LOGGER.warn("{} LuckPerms support is enabled in config, but LuckPerms is not installed.", LOG_PREFIX);
            } else if (config.useLuckPerms) {
                LOGGER.debug("{} LuckPerms support enabled and detected.", LOG_PREFIX);
            }

            UpdateChecker.checkForUpdates();
        });
    }

    public static EggifyConfig loadConfigForEditing() {
        return CONFIG.loadConfigForEditing();
    }

    public static EggifyConfig applyEditedConfig(EggifyConfig editedConfig) {
        return CONFIG.applyEditedConfig(editedConfig);
    }
}
