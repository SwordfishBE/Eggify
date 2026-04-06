package net.eggify;

import java.util.ArrayList;
import java.util.List;
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
        ConfigCategory specialEgg = builder.getOrCreateCategory(Component.literal("Special Egg"));
        ConfigCategory permissions = builder.getOrCreateCategory(Component.literal("Permissions"));
        ConfigCategory rarity = builder.getOrCreateCategory(Component.literal("Rarity"));
        ConfigCategory bosses = builder.getOrCreateCategory(Component.literal("Bosses"));
        ConfigCategory blacklist = builder.getOrCreateCategory(Component.literal("Blacklist"));

        rarity.addEntry(entries.startDoubleField(Component.literal("Passive Drop Chance Percent"), config.passiveDropChancePercent)
            .setDefaultValue(30.0D)
            .setMin(0.0D)
            .setMax(100.0D)
            .setTooltip(Component.literal("Chance that a thrown egg turns a passive mob into its spawn egg."))
            .setSaveConsumer(value -> config.passiveDropChancePercent = value)
            .build());

        rarity.addEntry(entries.startDoubleField(Component.literal("Hostile Drop Chance Percent"), config.hostileDropChancePercent)
            .setDefaultValue(15.0D)
            .setMin(0.0D)
            .setMax(100.0D)
            .setTooltip(Component.literal("Chance that a thrown egg turns a hostile mob into its spawn egg."))
            .setSaveConsumer(value -> config.hostileDropChancePercent = value)
            .build());

        rarity.addEntry(entries.startDoubleField(Component.literal("Boss Drop Chance Percent"), config.bossDropChancePercent)
            .setDefaultValue(5.0D)
            .setMin(0.0D)
            .setMax(100.0D)
            .setTooltip(Component.literal("Chance that a thrown egg turns a configured boss mob into its spawn egg."))
            .setSaveConsumer(value -> config.bossDropChancePercent = value)
            .build());

        specialEgg.addEntry(entries.startBooleanToggle(Component.literal("Enable Egg of No Escape"), config.enableSpecialEgg)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enables the special guaranteed-capture egg recipe and item behavior."))
            .setSaveConsumer(value -> config.enableSpecialEgg = value)
            .build());

        specialEgg.addEntry(entries.startDoubleField(Component.literal("Special Egg Recovery Chance Percent"), config.specialEggRecoveryChancePercent)
            .setDefaultValue(50.0D)
            .setMin(0.0D)
            .setMax(100.0D)
            .setTooltip(Component.literal("Chance that a missed Egg of No Escape drops back intact instead of breaking."))
            .setSaveConsumer(value -> config.specialEggRecoveryChancePercent = value)
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

        bosses.addEntry(entries.startStrList(Component.literal("Boss Mobs"), new ArrayList<>(config.bossMobs))
            .setDefaultValue(new ArrayList<>(List.of("minecraft:ender_dragon", "minecraft:wither")))
            .setTooltip(Component.literal("Use full namespaced mob IDs that should use the boss rarity chance."))
            .setSaveConsumer(value -> config.bossMobs = new ArrayList<>(value))
            .build());

        blacklist.addEntry(entries.startStrList(Component.literal("Blacklisted Mobs"), new ArrayList<>(config.blacklistedMobs))
            .setDefaultValue(new ArrayList<>())
            .setTooltip(Component.literal("Use full namespaced mob IDs, for example minecraft:wither."))
            .setSaveConsumer(value -> config.blacklistedMobs = new ArrayList<>(value))
            .build());

        return builder.build();
    }
}
