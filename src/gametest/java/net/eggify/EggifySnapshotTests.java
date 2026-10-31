package net.eggify;

import java.lang.reflect.Method;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EggifySnapshotTests {
    @GameTest
    public void frostbiteCaptureAndReleasePreservesBabyVariant(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var frostbite = helper.spawn(EntityTypes.FROSTBITE, new BlockPos(1, 2, 1));
        frostbite.setBaby(true);
        var egg = new ThrownEgg(helper.getLevel(), player, SpecialEggHelper.createSpecialEgg());
        var bounds = frostbite.getBoundingBox().inflate(1);
        helper.assertTrue(EggifyMobHandler.tryEggify(egg, frostbite), "The new Frostbite must be capturable");
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, bounds,
            item -> item.getItem().is(Items.FROSTBITE_SPAWN_EGG));
        helper.assertTrue(frostbite.isRemoved() && drops.size() == 1, "Capture must produce one Frostbite egg");
        var released = EntityTypes.FROSTBITE.spawn(helper.getLevel(), drops.getFirst().getItem(), player,
            helper.absolutePos(new BlockPos(2, 2, 1)), EntitySpawnReason.SPAWN_ITEM_USE, false, false);
        helper.assertTrue(released != null && released.isBaby(), "Released Frostbite must remain a baby");
        helper.succeed();
    }

    @GameTest
    public void coldSulfurCubeCaptureAndReleasePreservesArchetype(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        var cube = helper.spawn(EntityTypes.SULFUR_CUBE, new BlockPos(1, 2, 1));
        helper.assertTrue(cube.equipItem(new ItemStack(Items.BLUE_ICE)), "Cube must absorb Blue Ice");
        var content = cube.get(DataComponents.SULFUR_CUBE_CONTENT);
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.SULFUR_CUBE_ARCHETYPE);
        var cold = cube.matchingArchetypes(new ItemStack(Items.BLUE_ICE)).stream()
            .filter(archetype -> registry.getKey(archetype).toString().equals("minecraft:cold"))
            .findFirst();
        helper.assertTrue(cold.isPresent(), "Blue Ice must select the new Cold archetype");
        helper.assertTrue(!cold.orElseThrow().locationBasedEffects().isEmpty(), "Cold must have location effects");
        var bounds = cube.getBoundingBox().inflate(1);
        helper.assertTrue(EggifyMobHandler.tryEggify(
            new ThrownEgg(helper.getLevel(), player, SpecialEggHelper.createSpecialEgg()), cube),
            "Cold Sulfur Cube must be capturable");
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, bounds,
            item -> item.getItem().is(Items.SULFUR_CUBE_SPAWN_EGG));
        helper.assertTrue(drops.size() == 1, "Capture must drop one cube spawn egg");
        var released = EntityTypes.SULFUR_CUBE.spawn(helper.getLevel(), drops.getFirst().getItem(), player,
            helper.absolutePos(new BlockPos(2, 2, 1)), EntitySpawnReason.SPAWN_ITEM_USE, false, false);
        helper.assertTrue(released != null && content.equals(released.get(DataComponents.SULFUR_CUBE_CONTENT)),
            "Released cube must still contain Blue Ice");
        helper.assertTrue(released.matchingArchetypes(new ItemStack(Items.BLUE_ICE)).contains(cold.orElseThrow()),
            "The Cold archetype must still apply after release");
        helper.succeed();
    }

    @GameTest
    public void updateCheckerUsesRawMinecraftId(GameTestHelper helper) throws Exception {
        Method currentVersion = UpdateChecker.class.getDeclaredMethod("currentMinecraftVersion");
        currentVersion.setAccessible(true);
        helper.assertTrue(currentVersion.invoke(null).equals(java.util.Optional.of(SharedConstants.getCurrentVersion().id())),
            "Update checks must use the game's raw snapshot ID rather than Fabric's normalized version");
        helper.succeed();
    }
}
