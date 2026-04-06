package net.eggify.command;

import net.eggify.EggifyMod;
import net.eggify.PermissionHelper;
import net.eggify.config.EggifyConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TypedEntityData;

import java.util.StringJoiner;

public final class EggifyCommand {
    private EggifyCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(buildRoot()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildRoot() {
        return Commands.literal("eggify")
            .then(Commands.literal("info")
                .requires(PermissionHelper::canUseCommand)
                .executes(context -> showInfo(context.getSource())))
            .then(Commands.literal("held")
                .requires(PermissionHelper::canDebugCommand)
                .executes(context -> showHeldItemInfo(context.getSource())))
            .then(Commands.literal("reload")
                .requires(PermissionHelper::canReloadCommand)
                .executes(context -> reloadConfig(context.getSource())))
            .executes(context -> Command.SINGLE_SUCCESS);
    }

    private static int reloadConfig(CommandSourceStack source) {
        EggifyMod.CONFIG.load();
        EggifyMod.LOGGER.info("{} Config reloaded via command.", EggifyMod.LOG_PREFIX);
        source.sendSuccess(() -> Component.literal("Eggify config reloaded."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int showInfo(CommandSourceStack source) {
        EggifyConfig config = EggifyMod.CONFIG.getConfig();
        boolean luckPermsInstalled = PermissionHelper.isLuckPermsInstalled();
        boolean usingLuckPermsPermissions = PermissionHelper.usesLuckPermsPermissions(config);
        String bosses = config.bossMobs.isEmpty() ? "none" : String.join(", ", config.bossMobs);
        String blacklist = config.blacklistedMobs.isEmpty() ? "none" : String.join(", ", config.blacklistedMobs);

        source.sendSuccess(() -> Component.literal("Eggify configuration"), false);
        source.sendSuccess(() -> Component.literal("Passive drop chance: " + config.passiveDropChancePercent + "%"), false);
        source.sendSuccess(() -> Component.literal("Hostile drop chance: " + config.hostileDropChancePercent + "%"), false);
        source.sendSuccess(() -> Component.literal("Boss drop chance: " + config.bossDropChancePercent + "%"), false);
        source.sendSuccess(() -> Component.literal("LuckPerms mode enabled: " + config.useLuckPerms), false);
        source.sendSuccess(() -> Component.literal("LuckPerms detected: " + luckPermsInstalled), false);
        source.sendSuccess(() -> Component.literal("LuckPerms permissions active: " + usingLuckPermsPermissions), false);
        source.sendSuccess(() -> Component.literal("Command permission node enabled: " + config.allowCommandPermissionNode + (usingLuckPermsPermissions ? " (ignored while LuckPerms is active)" : "")), false);
        source.sendSuccess(() -> Component.literal("Held debug command enabled: " + config.allowDebugCommand + (usingLuckPermsPermissions ? " (ignored while LuckPerms is active)" : "")), false);
        source.sendSuccess(() -> Component.literal("Special egg enabled: " + config.enableSpecialEgg), false);
        source.sendSuccess(() -> Component.literal("Special egg recovery chance: " + config.specialEggRecoveryChancePercent + "%"), false);
        source.sendSuccess(() -> Component.literal("Permission nodes: " + PermissionHelper.USE_PERMISSION + ", " + PermissionHelper.COMMAND_PERMISSION + ", " + PermissionHelper.DEBUG_PERMISSION + ", " + PermissionHelper.SPECIAL_EGG_CRAFT_PERMISSION + ", " + PermissionHelper.SPECIAL_EGG_USE_PERMISSION), false);
        source.sendSuccess(() -> Component.literal("Boss mobs: " + bosses), false);
        source.sendSuccess(() -> Component.literal("Blacklist: " + blacklist), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int showHeldItemInfo(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("Your main hand is empty."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Eggify held item debug"), false);
        source.sendSuccess(() -> Component.literal("Item: " + BuiltInRegistries.ITEM.getKey(stack.getItem())), false);
        source.sendSuccess(() -> Component.literal("Name: " + stack.getHoverName().getString()), false);

        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) {
            source.sendSuccess(() -> Component.literal("Lore: none"), false);
        } else {
            source.sendSuccess(() -> Component.literal("Lore: present"), false);
            for (Component line : lore.lines()) {
                source.sendSuccess(() -> Component.literal(" - " + line.getString()), false);
            }
        }

        TypedEntityData<?> entityData = stack.get(DataComponents.ENTITY_DATA);
        if (entityData == null) {
            source.sendSuccess(() -> Component.literal("Entity data: none"), false);
            return Command.SINGLE_SUCCESS;
        }

        CompoundTag entityTag = entityData.copyTagWithoutId();
        StringJoiner joiner = new StringJoiner(", ");
        for (String key : entityTag.keySet()) {
            joiner.add(key);
        }

        source.sendSuccess(() -> Component.literal("Entity type: " + entityData.type()), false);
        source.sendSuccess(() -> Component.literal("Entity data keys: " + (joiner.length() == 0 ? "none" : joiner.toString())), false);
        source.sendSuccess(() -> Component.literal("Entity data NBT: " + entityTag), false);
        return Command.SINGLE_SUCCESS;
    }
}
