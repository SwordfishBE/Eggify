package net.eggify.mixin;

import net.eggify.PermissionHelper;
import net.eggify.SpecialEggHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ResultSlot.class)
abstract class ResultSlotMixin extends Slot {
    @Shadow
    @Final
    private CraftingContainer craftSlots;

    protected ResultSlotMixin(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPickup(Player player) {
        return super.mayPickup(player)
            && (!(SpecialEggHelper.isSpecialEgg(getItem())
                || SpecialEggHelper.matchesSpecialEggRecipe(this.craftSlots))
                || PermissionHelper.canCraftSpecialEgg(player));
    }
}
