package net.eggify.mixin;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.CrafterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrafterBlock.class)
abstract class CrafterBlockMixin {
    @Inject(method = "getPotentialResults", at = @At("RETURN"), cancellable = true)
    private static void eggify$requirePlayerCrafting(ServerLevel level, CraftingInput input,
            CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> callbackInfo) {
        // Automated crafting has no player whose craft permission can be checked.
        if (callbackInfo.getReturnValue().filter(recipe ->
                recipe.id().identifier().toString().equals("eggify:egg_of_no_escape")).isPresent()) {
            callbackInfo.setReturnValue(Optional.empty());
        }
    }
}
