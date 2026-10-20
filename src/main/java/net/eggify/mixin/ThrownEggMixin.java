package net.eggify.mixin;

import net.eggify.EggifyMobHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEgg.class)
abstract class ThrownEggMixin extends ThrowableItemProjectile {
    protected ThrownEggMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void eggify$onHit(HitResult result, CallbackInfo callbackInfo) {
        ThrownEgg egg = (ThrownEgg) (Object) this;
        boolean captured = result instanceof EntityHitResult entityHitResult
            && EggifyMobHandler.tryEggify(egg, entityHitResult.getEntity());
        if (captured || EggifyMobHandler.handleSpecialMiss(egg)) {
            this.discard();
            callbackInfo.cancel();
        }
    }
}
