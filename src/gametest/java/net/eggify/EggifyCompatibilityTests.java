package net.eggify;

import java.lang.reflect.Method;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;

public class EggifyCompatibilityTests {
    @GameTest
    public void requestedOptimizationModIsLoaded(GameTestHelper helper) {
        String expected = System.getProperty("eggify.compat.expectedMoonrise");
        if (expected != null) {
            var moonrise = FabricLoader.getInstance().getModContainer("moonrise");
            helper.assertTrue(moonrise.isPresent(), "Moonrise must actually be loaded for this compatibility run");
            helper.assertTrue(moonrise.orElseThrow().getMetadata().getVersion().getFriendlyString().equals(expected),
                "The requested Moonrise version must be loaded");
        }
        helper.succeed();
    }

    @GameTest(maxTicks = 80)
    public void flyingSpecialEggCapturesAndDropsExactlyOneSpawnEgg(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockServerPlayerInLevel();
        Vec3 target = helper.absoluteVec(new Vec3(3, 2, 1));
        var sheep = helper.spawn(EntityTypes.SHEEP, new Vec3(3, 2, 1));
        sheep.setNoAi(true);
        sheep.setNoGravity(true);
        var egg = new ThrownEgg(level, player, SpecialEggHelper.createSpecialEgg());
        egg.setPos(target.x - 2, target.y + 0.6, target.z);
        egg.setNoGravity(true);
        egg.setDeltaMovement(0.6, 0, 0);
        helper.assertTrue(level.addFreshEntity(egg), "Thrown egg must enter the real entity tick loop");
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(sheep.isRemoved(), "The moving egg must capture the sheep via projectile collision");
            helper.assertTrue(egg.isRemoved(), "Successful capture must remove the projectile");
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(target, target).inflate(2),
                item -> item.getItem().is(Items.SHEEP_SPAWN_EGG));
            helper.assertTrue(drops.size() == 1 && drops.getFirst().getItem().getCount() == 1,
                "Capture must register exactly one spawn egg in Moonrise's entity lookup");
            helper.assertTrue(drops.getFirst().getItem().get(DataComponents.ENTITY_DATA) != null,
                "The dropped spawn egg must retain entity data");
            helper.succeed();
        });
    }

    @GameTest
    public void specialMissRecoveryHasExactZeroAndHundredPercentBoundaries(GameTestHelper helper) throws Exception {
        var config = EggifyMod.CONFIG.getConfig();
        double originalChance = config.specialEggRecoveryChancePercent;
        var level = helper.getLevel();
        Vec3 position = helper.absoluteVec(new Vec3(1, 2, 1));
        Method onHit = ThrownEgg.class.getDeclaredMethod("onHit", HitResult.class);
        onHit.setAccessible(true);
        try {
            for (double chance : new double[] {0, 100}) {
                config.specialEggRecoveryChancePercent = chance;
                var egg = new ThrownEgg(level, position.x, position.y, position.z, SpecialEggHelper.createSpecialEgg());
                helper.assertTrue(level.addFreshEntity(egg), "Test projectile must be registered");
                onHit.invoke(egg, new BlockHitResult(position, Direction.UP, BlockPos.containing(position), false));
                helper.assertTrue(egg.isRemoved(), "A missed special egg must remove the projectile");
                var recovered = level.getEntitiesOfClass(ItemEntity.class, new AABB(position, position).inflate(1),
                    item -> SpecialEggHelper.isSpecialEgg(item.getItem()));
                helper.assertTrue(recovered.size() == (chance == 100 ? 1 : 0),
                    "Miss recovery must match the configured boundary");
                recovered.forEach(ItemEntity::discard);
            }
        } finally {
            config.specialEggRecoveryChancePercent = originalChance;
        }
        helper.succeed();
    }
}
