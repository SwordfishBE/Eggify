package com.eggify.mixin;

import com.eggify.EggifyMobHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEgg.class)
abstract class ThrownEggMixin extends ThrowableItemProjectile {
    @Unique
    private boolean eggify$successfulHit;

    protected ThrownEggMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void eggify$onHitEntity(EntityHitResult result, CallbackInfo callbackInfo) {
        if (EggifyMobHandler.tryEggify((ThrownEgg) (Object) this, result.getEntity())) {
            this.eggify$successfulHit = true;
            callbackInfo.cancel();
        }
    }

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void eggify$onHit(HitResult result, CallbackInfo callbackInfo) {
        if (!this.eggify$successfulHit && result instanceof EntityHitResult entityHitResult) {
            if (EggifyMobHandler.tryEggify((ThrownEgg) (Object) this, entityHitResult.getEntity())) {
                this.eggify$successfulHit = true;
            }
        }

        if (this.eggify$successfulHit) {
            this.discard();
            callbackInfo.cancel();
        }
    }
}
