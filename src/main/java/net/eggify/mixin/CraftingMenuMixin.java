package net.eggify.mixin;

import net.eggify.SpecialEggHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
abstract class CraftingMenuMixin {
    @Inject(method = "slotChangedCraftingGrid", at = @At("RETURN"))
    private static void eggify$updateSpecialEggResult(AbstractContainerMenu menu, ServerLevel serverLevel, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer, RecipeHolder<?> recipeHolder, CallbackInfo callbackInfo) {
        if (!SpecialEggHelper.matchesSpecialEggRecipe(craftingContainer)) {
            return;
        }

        ItemStack resultStack = resultContainer.getItem(0);
        if (resultStack.isEmpty()) {
            return;
        }

        resultStack = SpecialEggHelper.applyCraftingResult(player, craftingContainer, resultStack);
        resultContainer.setItem(0, resultStack);
        menu.broadcastChanges();
    }
}
