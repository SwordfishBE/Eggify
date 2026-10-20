package net.eggify;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EggifyGameTests {
    @GameTest
    public void crafterRejectsSpecialEggButStillCraftsOrdinaryRecipes(GameTestHelper helper) {
        List<ItemStack> ingredients = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ingredients.add(new ItemStack(i == 4 ? Items.EGG : Items.DIAMOND));
        }
        helper.assertTrue(CrafterBlock.getPotentialResults(helper.getLevel(),
            CraftingInput.of(3, 3, ingredients)).isEmpty(), "Crafter must reject the special recipe");
        helper.assertTrue(CrafterBlock.getPotentialResults(helper.getLevel(),
            CraftingInput.of(1, 1, List.of(new ItemStack(Items.OAK_LOG)))).isPresent(),
            "Ordinary recipes must remain available");
        helper.succeed();
    }

    @GameTest
    public void revokedCraftingAccessBlocksNormalAndShiftClick(GameTestHelper helper) {
        var config = EggifyMod.CONFIG.getConfig();
        boolean enabled = config.enableSpecialEgg;
        boolean permissions = config.useLuckPerms;
        try {
            config.enableSpecialEgg = true;
            config.useLuckPerms = false;
            var player = helper.makeMockServerPlayerInLevel();
            var menu = new CraftingMenu(1, player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(BlockPos.ZERO)));
            for (int i = 0; i < 9; i++) {
                menu.getInputGridSlots().get(i).set(new ItemStack(i == 4 ? Items.EGG : Items.DIAMOND, 2));
            }
            helper.assertTrue(SpecialEggHelper.isSpecialEgg(menu.getResultSlot().getItem()),
                "The player recipe must produce a working special egg");
            helper.assertFalse(menu.quickMoveStack(player, 0).isEmpty(), "Allowed shift-click must work");
            helper.assertTrue(menu.getInputGridSlots().getFirst().getItem().getCount() == 1,
                "Shift-click must consume exactly one recipe");
            config.enableSpecialEgg = false;
            helper.assertFalse(menu.getResultSlot().mayPickup(player), "Normal pickup must be denied");
            helper.assertTrue(menu.quickMoveStack(player, 0).isEmpty(), "Shift-click must be denied");
            helper.assertTrue(menu.getInputGridSlots().getFirst().getItem().getCount() == 1,
                "Denied pickup must not consume ingredients");
        } finally {
            config.enableSpecialEgg = enabled;
            config.useLuckPerms = permissions;
        }
        helper.succeed();
    }

    @GameTest
    public void failedCaptureDoesNotRollAgain(GameTestHelper helper) throws Exception {
        var config = EggifyMod.CONFIG.getConfig();
        double chance = config.passiveDropChancePercent;
        boolean permissions = config.useLuckPerms;
        try {
            config.passiveDropChancePercent = 30;
            config.useLuckPerms = false;
            var player = helper.makeMockServerPlayerInLevel();
            var sheep = helper.spawn(EntityTypes.SHEEP, new BlockPos(1, 2, 1));
            var egg = new ThrownEgg(helper.getLevel(), player, new ItemStack(Items.EGG));
            var random = helper.getLevel().getRandom();
            long seed = 0;
            for (; seed < 10000; seed++) {
                random.setSeed(seed);
                if (random.nextDouble() >= 0.3 && random.nextDouble() < 0.3) {
                    break;
                }
            }
            helper.assertTrue(seed < 10000, "A deterministic regression seed must exist");
            Method onHit = ThrownEgg.class.getDeclaredMethod("onHit", HitResult.class);
            onHit.setAccessible(true);
            random.setSeed(seed);
            onHit.invoke(egg, new EntityHitResult(sheep));
            helper.assertTrue(sheep.isAlive(), "A failed first roll must not be followed by a second capture roll");
        } finally {
            config.passiveDropChancePercent = chance;
            config.useLuckPerms = permissions;
        }
        helper.succeed();
    }

    @GameTest
    public void sanitizationDropsRuntimeFieldsAndKeepsVariants(GameTestHelper helper) throws Exception {
        CompoundTag original = new CompoundTag();
        for (String key : List.of("leash", "equipment", "attributes", "drop_chances", "UUID", "Pos", "Health",
                "sleeping_pos", "fall_distance", "home_pos", "home_radius", "last_hurt_by_player",
                "invulnerable_time", "current_explosion_impact_pos")) {
            original.putString(key, "runtime");
        }
        original.putString("variant", "minecraft:snowy");
        original.putInt("Age", -12000);
        Method sanitize = EggifyMobHandler.class.getDeclaredMethod("sanitizeEntityDataForSpawnEgg", CompoundTag.class);
        sanitize.setAccessible(true);
        CompoundTag result = (CompoundTag) sanitize.invoke(null, original);
        helper.assertTrue(result.size() == 2, "Only variant and age should remain");
        helper.assertTrue(result.getString("variant").orElseThrow().equals("minecraft:snowy"), "Variant must survive");
        helper.assertTrue(result.getInt("Age").orElseThrow() == -12000, "Baby age must survive");
        helper.assertTrue(original.contains("equipment"), "Sanitization must not mutate its input");
        helper.succeed();
    }

    @GameTest
    public void sulfurCubeKeepsAbsorbedBlockWhenEquipmentIsFiltered(GameTestHelper helper) throws Exception {
        var cube = helper.spawn(EntityTypes.SULFUR_CUBE, new BlockPos(1, 2, 1));
        helper.assertTrue(cube.equipItem(new ItemStack(Items.BLUE_ICE)), "Cube must absorb the test block");
        var content = cube.get(DataComponents.SULFUR_CUBE_CONTENT);
        helper.assertTrue(content != null, "Cube must expose absorbed content");
        Method createDrop = EggifyMobHandler.class.getDeclaredMethod("createSpawnEggDrop", Mob.class, ItemStack.class);
        createDrop.setAccessible(true);
        ItemStack drop = (ItemStack) createDrop.invoke(null, cube, new ItemStack(Items.SULFUR_CUBE_SPAWN_EGG));
        helper.assertTrue(content.equals(drop.get(DataComponents.SULFUR_CUBE_CONTENT)),
            "Absorbed block must survive capture separately from equipment");
        helper.assertFalse(drop.get(DataComponents.ENTITY_DATA).copyTagWithoutId().contains("equipment"),
            "Raw equipment must still be filtered");
        helper.succeed();
    }
}
