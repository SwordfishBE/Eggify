package net.eggify.config;

import net.eggify.EggifyMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("eggify.json");
    private static final String NEW_LINE = System.lineSeparator();

    private EggifyConfig config;

    public EggifyConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String rawConfig = Files.readString(CONFIG_PATH);
                this.config = GSON.fromJson(stripJsonComments(rawConfig), EggifyConfig.class);
            } catch (IOException exception) {
                EggifyMod.LOGGER.error("{} Failed to read config, using defaults.", EggifyMod.LOG_PREFIX, exception);
            }
        }

        if (this.config == null) {
            this.config = new EggifyConfig();
        }

        this.config.sanitize();
        this.save();
        return this.config;
    }

    public EggifyConfig getConfig() {
        if (this.config == null) {
            return this.load();
        }

        return this.config;
    }

    public EggifyConfig loadConfigForEditing() {
        EggifyConfig loadedConfig = this.getConfig();
        return GSON.fromJson(GSON.toJson(loadedConfig), EggifyConfig.class);
    }

    public EggifyConfig applyEditedConfig(EggifyConfig editedConfig) {
        this.config = editedConfig == null ? new EggifyConfig() : editedConfig;
        this.config.sanitize();
        this.save();
        return this.config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, writeConfigWithComments(this.getConfig()));
        } catch (IOException exception) {
            EggifyMod.LOGGER.error("{} Failed to write config.", EggifyMod.LOG_PREFIX, exception);
        }
    }

    private static String writeConfigWithComments(EggifyConfig config) {
        StringBuilder builder = new StringBuilder();
        builder.append("// Eggify configuration").append(NEW_LINE);
        builder.append("// This file supports // comments.").append(NEW_LINE);
        builder.append("// Reload changes with /eggify reload.").append(NEW_LINE);
        builder.append('{').append(NEW_LINE);
        builder.append("  // Chance in percent that a thrown egg converts a mob into its spawn egg.").append(NEW_LINE);
        builder.append("  // Default is intentionally low but still realistic for survival gameplay.").append(NEW_LINE);
        builder.append("  \"dropChancePercent\": ").append(formatDouble(config.dropChancePercent)).append(',').append(NEW_LINE);
        builder.append(NEW_LINE);
        builder.append("  // When true, Eggify requires LuckPerms + fabric-permissions-api permission nodes.").append(NEW_LINE);
        builder.append("  // When false, every player can use Eggify drops.").append(NEW_LINE);
        builder.append("  \"useLuckPerms\": ").append(config.useLuckPerms).append(',').append(NEW_LINE);
        builder.append(NEW_LINE);
        builder.append("  // When true, non-OP players can use /eggify info through the eggify.command node.").append(NEW_LINE);
        builder.append("  // Ignored when LuckPerms is enabled and installed.").append(NEW_LINE);
        builder.append("  \"allowCommandPermissionNode\": ").append(config.allowCommandPermissionNode).append(',').append(NEW_LINE);
        builder.append(NEW_LINE);
        builder.append("  // When true, non-OP players can use /eggify held without LuckPerms.").append(NEW_LINE);
        builder.append("  // Ignored when LuckPerms is enabled and installed.").append(NEW_LINE);
        builder.append("  \"allowDebugCommand\": ").append(config.allowDebugCommand).append(',').append(NEW_LINE);
        builder.append(NEW_LINE);
        builder.append("  // /eggify reload always stays OP-only.").append(NEW_LINE);
        builder.append("  // Enables the Modrinth update check.").append(NEW_LINE);
        builder.append("  \"enableUpdateCheck\": ").append(config.enableUpdateCheck).append(',').append(NEW_LINE);
        builder.append(NEW_LINE);
        builder.append("  // Mobs in this list can never be eggified. Use full namespaced IDs.").append(NEW_LINE);
        builder.append("  \"blacklistedMobs\": [").append(NEW_LINE);
        for (int index = 0; index < config.blacklistedMobs.size(); index++) {
            String mobId = config.blacklistedMobs.get(index);
            builder.append("    ").append(GSON.toJson(mobId));
            if (index < config.blacklistedMobs.size() - 1) {
                builder.append(',');
            }
            builder.append(NEW_LINE);
        }
        builder.append("  ]").append(NEW_LINE);
        builder.append('}').append(NEW_LINE);
        return builder.toString();
    }

    private static String formatDouble(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }

        return Double.toString(value);
    }

    private static String stripJsonComments(String raw) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaping = false;
        boolean lineComment = false;
        boolean blockComment = false;

        for (int index = 0; index < raw.length(); index++) {
            char current = raw.charAt(index);
            char next = index + 1 < raw.length() ? raw.charAt(index + 1) : '\0';

            if (lineComment) {
                if (current == '\n' || current == '\r') {
                    lineComment = false;
                    result.append(current);
                }
                continue;
            }

            if (blockComment) {
                if (current == '*' && next == '/') {
                    blockComment = false;
                    index++;
                }
                continue;
            }

            if (inString) {
                result.append(current);
                if (escaping) {
                    escaping = false;
                } else if (current == '\\') {
                    escaping = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
                result.append(current);
                continue;
            }

            if (current == '/' && next == '/') {
                lineComment = true;
                index++;
                continue;
            }

            if (current == '/' && next == '*') {
                blockComment = true;
                index++;
                continue;
            }

            result.append(current);
        }

        return result.toString();
    }
}
