package net.eggify;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public final class SpecialEggHelper {
    private static final String SPECIAL_EGG_TAG = "EggifySpecial";
    private static final Component SPECIAL_EGG_NAME = Component.literal("Egg of No Escape")
        .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true).withItalic(false));
    private static final Component SPECIAL_EGG_LORE = Component.literal("A hit means capture. Always.")
        .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false));

    private SpecialEggHelper() {
    }

    public static ItemStack createSpecialEgg() {
        ItemStack stack = new ItemStack(Items.EGG);
        applySpecialEggComponents(stack);
        return stack;
    }

    public static void applySpecialEggComponents(ItemStack stack) {
        CompoundTag customTag = new CompoundTag();
        customTag.putBoolean(SPECIAL_EGG_TAG, true);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customTag));
        stack.set(DataComponents.CUSTOM_NAME, SPECIAL_EGG_NAME);
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            SPECIAL_EGG_LORE
        )));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        stack.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.ENCHANTMENTS, true));
    }

    public static boolean isSpecialEgg(ItemStack stack) {
        if (!stack.is(Items.EGG)) {
            return false;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return false;
        }

        return customData.copyTag().getBoolean(SPECIAL_EGG_TAG).orElse(false);
    }

    public static boolean matchesSpecialEggRecipe(CraftingContainer craftingContainer) {
        if (craftingContainer.getContainerSize() != 9) {
            return false;
        }

        for (int index = 0; index < craftingContainer.getContainerSize(); index++) {
            ItemStack stack = craftingContainer.getItem(index);
            if (index == 4) {
                if (stack.isEmpty() || !isAllowedBaseEgg(stack)) {
                    return false;
                }
                continue;
            }

            if (!stack.is(Items.DIAMOND)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isAllowedBaseEgg(ItemStack stack) {
        return stack.is(Items.EGG) || stack.is(Items.BLUE_EGG) || stack.is(Items.BROWN_EGG);
    }

    public static ItemStack applyCraftingResult(Player player, CraftingContainer craftingContainer, ItemStack resultStack) {
        if (!matchesSpecialEggRecipe(craftingContainer)) {
            return resultStack;
        }

        if (!PermissionHelper.canCraftSpecialEgg(player)) {
            return ItemStack.EMPTY;
        }

        applySpecialEggComponents(resultStack);
        resultStack.setCount(1);
        return resultStack;
    }

    public static InteractionResult handleUse(Player player, Level level, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isSpecialEgg(stack)) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        if (PermissionHelper.canUseSpecialEgg(serverPlayer)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            if (!EggifyMod.CONFIG.getConfig().enableSpecialEgg) {
                serverPlayer.sendSystemMessage(Component.literal("The Egg of No Escape is disabled on this server."));
            } else {
                serverPlayer.sendSystemMessage(Component.literal("You do not have permission to use the Egg of No Escape."));
            }
        }

        return InteractionResult.FAIL;
    }
}
