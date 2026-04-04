package net.eggify;

import java.util.ArrayList;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.eggify.config.EggifyConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class EggifyClothConfigScreen {
    private EggifyClothConfigScreen() {
    }

    static Screen create(Screen parent) {
        EggifyConfig config = EggifyMod.loadConfigForEditing();

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Eggify Config"))
            .setSavingRunnable(() -> EggifyMod.applyEditedConfig(config));

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        ConfigCategory permissions = builder.getOrCreateCategory(Component.literal("Permissions"));
        ConfigCategory blacklist = builder.getOrCreateCategory(Component.literal("Blacklist"));

        general.addEntry(entries.startDoubleField(Component.literal("Drop Chance Percent"), config.dropChancePercent)
            .setDefaultValue(2.5D)
            .setMin(0.0D)
            .setMax(100.0D)
            .setTooltip(Component.literal("Chance that a thrown egg turns a mob into its spawn egg."))
            .setSaveConsumer(value -> config.dropChancePercent = value)
            .build());

        general.addEntry(entries.startBooleanToggle(Component.literal("Enable Update Check"), config.enableUpdateCheck)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Checks Modrinth for newer Eggify releases."))
            .setSaveConsumer(value -> config.enableUpdateCheck = value)
            .build());

        permissions.addEntry(entries.startBooleanToggle(Component.literal("Use LuckPerms"), config.useLuckPerms)
            .setDefaultValue(false)
            .setTooltip(Component.literal("Requires LuckPerms permission nodes for Eggify access."))
            .setSaveConsumer(value -> config.useLuckPerms = value)
            .build());

        permissions.addEntry(entries.startBooleanToggle(Component.literal("Allow Command Permission Node"), config.allowCommandPermissionNode)
            .setDefaultValue(false)
            .setTooltip(Component.literal("Allows /eggify info for non-OP players without LuckPerms."))
            .setSaveConsumer(value -> config.allowCommandPermissionNode = value)
            .build());

        permissions.addEntry(entries.startBooleanToggle(Component.literal("Allow Debug Command"), config.allowDebugCommand)
            .setDefaultValue(false)
            .setTooltip(Component.literal("Allows /eggify held for non-OP players without LuckPerms."))
            .setSaveConsumer(value -> config.allowDebugCommand = value)
            .build());

        blacklist.addEntry(entries.startStrList(Component.literal("Blacklisted Mobs"), new ArrayList<>(config.blacklistedMobs))
            .setDefaultValue(new ArrayList<>())
            .setTooltip(Component.literal("Use full namespaced mob IDs, for example minecraft:wither."))
            .setSaveConsumer(value -> config.blacklistedMobs = new ArrayList<>(value))
            .build());

        return builder.build();
    }
}
