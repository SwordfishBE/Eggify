package net.eggify.mixin;

import net.eggify.PermissionHelper;
import net.eggify.SpecialEggHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
abstract class ResultSlotMixin {
    @Shadow
    public abstract ItemStack getItem();

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void eggify$checkResultPickup(Player player, CallbackInfoReturnable<Boolean> callbackInfo) {
        if ((Object) this instanceof ResultSlot && SpecialEggHelper.isSpecialEgg(getItem())
                && !PermissionHelper.canCraftSpecialEgg(player)) {
            callbackInfo.setReturnValue(false);
        }
    }
}
