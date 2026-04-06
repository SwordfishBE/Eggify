package net.eggify.mixin;

import net.eggify.PermissionHelper;
import net.eggify.SpecialEggHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
abstract class ResultSlotMixin {
    @Shadow
    @Final
    private CraftingContainer craftSlots;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void eggify$applySpecialEggComponents(Player player, ItemStack stack, CallbackInfo callbackInfo) {
        if (!SpecialEggHelper.matchesSpecialEggRecipe(this.craftSlots)) {
            return;
        }

        if (!PermissionHelper.canCraftSpecialEgg(player)) {
            return;
        }

        SpecialEggHelper.applySpecialEggComponents(stack);
        stack.setCount(1);
    }
}
