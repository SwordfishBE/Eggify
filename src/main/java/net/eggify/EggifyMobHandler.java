package net.eggify;

import net.minecraft.advancements.criterion.NbtPredicate;
import net.eggify.config.EggifyConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EggifyMobHandler {
    private static final EntityType<?> VARIANT_DATA_EXCLUDED_TYPE = EntityType.TROPICAL_FISH;
    private static final String[] PARROT_VARIANT_NAMES = {
        "Red Blue",
        "Blue",
        "Green",
        "Yellow Blue",
        "Gray"
    };

    private EggifyMobHandler() {
    }

    public static boolean tryEggify(ThrownEgg egg, Entity target) {
        Level level = egg.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        if (!(egg.getOwner() instanceof ServerPlayer player)) {
            return false;
        }

        if (!(target instanceof Mob mob) || !mob.isAlive()) {
            return false;
        }

        if (!PermissionHelper.canEggify(player)) {
            return false;
        }

        EggifyConfig config = EggifyMod.CONFIG.getConfig();
        if (isBlacklisted(mob, config)) {
            return false;
        }

        ItemStack drop = SpawnEggItem.byId(mob.getType())
            .map(holder -> new ItemStack(holder.value()))
            .orElse(ItemStack.EMPTY);
        if (drop.isEmpty()) {
            return false;
        }

        double chance = config.dropChancePercent / 100.0D;
        if (serverLevel.getRandom().nextDouble() > chance) {
            return false;
        }

        drop = createSpawnEggDrop(mob, drop);

        serverLevel.sendParticles(ParticleTypes.PORTAL, mob.getX(), mob.getY(0.5D), mob.getZ(), 32, 0.4D, 0.8D, 0.4D, 0.2D);
        serverLevel.playSound(null, mob.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 0.9F, 0.9F + serverLevel.getRandom().nextFloat() * 0.2F);
        mob.spawnAtLocation(serverLevel, drop);
        mob.discard();
        EggifyMod.LOGGER.debug("{} {} eggified {}", EggifyMod.LOG_PREFIX, player.getName().getString(), BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()));
        return true;
    }

    private static boolean isBlacklisted(Mob mob, EggifyConfig config) {
        return config.blacklistedMobs.contains(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString());
    }

    private static ItemStack createSpawnEggDrop(Mob mob, ItemStack stack) {
        CompoundTag entityTag = null;
        if (mob.getType() == VARIANT_DATA_EXCLUDED_TYPE) {
            return stack;
        }

        try {
            entityTag = sanitizeEntityDataForSpawnEgg(NbtPredicate.getEntityTagToCompare(mob));
            stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(mob.getType(), entityTag));
        } catch (Exception exception) {
            EggifyMod.LOGGER.debug("{} Failed to attach variant entity data for {}", EggifyMod.LOG_PREFIX, BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()), exception);
        }

        addVariantTooltip(stack, mob, entityTag);
        return stack;
    }

    private static void addVariantTooltip(ItemStack stack, Mob mob, CompoundTag entityTag) {
        List<Component> lines = createVariantTooltipLines(mob, entityTag);
        if (!lines.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(lines));
        }
    }

    private static List<Component> createVariantTooltipLines(Mob mob, CompoundTag entityTag) {
        List<Component> lines = new ArrayList<>();
        addTooltipLine(lines, "Age", resolveAgeLabel(mob, entityTag));

        if (mob instanceof Villager villager) {
            addVillagerTooltipLines(lines, villager.getVillagerData());
            return lines;
        }

        if (mob instanceof ZombieVillager zombieVillager) {
            addVillagerTooltipLines(lines, zombieVillager.getVillagerData());
            return lines;
        }

        if (mob instanceof Sheep sheep) {
            addTooltipLine(lines, "Color", formatNameToken(sheep.getColor().getName()));
            return lines;
        }

        if (mob instanceof Goat goat) {
            addTooltipLine(lines, "Type", goat.isScreamingGoat() ? "Screaming" : "Normal");
            return lines;
        }

        if (mob instanceof Panda panda) {
            addTooltipLine(lines, "Personality", formatNameToken(getEnumLikeName(panda.getVariant())));
            return lines;
        }

        if (mob instanceof Llama llama) {
            if (llama.isTraderLlama()) {
                addTooltipLine(lines, "Type", "Trader");
            }
            addTooltipLine(lines, "Color", formatNameToken(getEnumLikeName(llama.getVariant())));
            return lines;
        }

        if (mob instanceof MushroomCow mushroomCow) {
            addTooltipLine(lines, "Type", formatNameToken(getEnumLikeName(mushroomCow.getVariant())));
            return lines;
        }

        if (mob instanceof Chicken chicken) {
            addTooltipLine(lines, "Type", formatHolderValue(chicken.getVariant()));
            return lines;
        }

        if (mob instanceof Cow cow) {
            addTooltipLine(lines, "Type", formatHolderValue(cow.getVariant()));
            return lines;
        }

        if (mob instanceof Rabbit rabbit) {
            addTooltipLine(lines, "Coat", formatNameToken(getEnumLikeName(rabbit.getVariant())));
            return lines;
        }

        if (mob instanceof Frog frog) {
            addTooltipLine(lines, "Type", formatHolderValue(frog.getVariant()));
            return lines;
        }

        if (mob instanceof Cat cat) {
            addTooltipLine(lines, "Coat", formatHolderValue(cat.getVariant()));
            return lines;
        }

        if (mob instanceof Axolotl axolotl) {
            addTooltipLine(lines, "Color", formatNameToken(getEnumLikeName(axolotl.getVariant())));
            return lines;
        }

        if (mob instanceof Wolf) {
            addTooltipLine(lines, "Coat", resolveVariantLabelFromEntityTag(mob, entityTag));
            return lines;
        }

        if (mob.getType() == EntityType.PARROT) {
            addTooltipLine(lines, "Color", resolveVariantLabelFromEntityTag(mob, entityTag));
            return lines;
        }

        String variantLabel = resolveVariantLabelFromEntityTag(mob, entityTag);
        if (variantLabel != null && !variantLabel.isBlank()) {
            lines.add(createTooltipLine("Variant: " + variantLabel));
        }

        return lines;
    }

    private static void addVillagerTooltipLines(List<Component> lines, VillagerData villagerData) {
        String biome = formatHolderValue(villagerData.type());
        if (biome != null && !biome.isBlank()) {
            lines.add(createTooltipLine("Biome: " + biome));
        }

        String profession = formatHolderValue(villagerData.profession());
        if (profession != null && !profession.isBlank()) {
            lines.add(createTooltipLine("Profession: " + profession));
        }

        lines.add(createTooltipLine("Level: " + formatVillagerLevel(villagerData.level())));
    }

    private static Component createTooltipLine(String text) {
        return Component.literal(text).withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false));
    }

    private static void addTooltipLine(List<Component> lines, String label, String value) {
        if (value != null && !value.isBlank()) {
            lines.add(createTooltipLine(label + ": " + value));
        }
    }

    private static String resolveAgeLabel(Mob mob, CompoundTag entityTag) {
        if (mob instanceof AgeableMob ageableMob) {
            return ageableMob.isBaby() ? "Baby" : "Adult";
        }

        if (entityTag == null) {
            return null;
        }

        return entityTag.getBoolean("IsBaby")
            .map(isBaby -> isBaby ? "Baby" : "Adult")
            .orElse(null);
    }

    private static String resolveVariantLabelFromEntityTag(Mob mob, CompoundTag entityTag) {
        if (entityTag == null) {
            return null;
        }

        String lowercaseVariant = entityTag.getString("variant")
            .map(EggifyMobHandler::formatNameToken)
            .orElse(null);
        if (lowercaseVariant != null) {
            return lowercaseVariant;
        }

        String uppercaseVariant = entityTag.getString("Variant")
            .map(EggifyMobHandler::formatNameToken)
            .orElse(null);
        if (uppercaseVariant != null) {
            return uppercaseVariant;
        }

        if (mob.getType() == EntityType.PARROT) {
            String parrotVariant = entityTag.getInt("Variant")
                .map(EggifyMobHandler::mapParrotVariant)
                .orElse(null);
            if (parrotVariant != null) {
                return parrotVariant;
            }
        }

        return entityTag.getString("Type")
            .map(EggifyMobHandler::formatNameToken)
            .orElse(null);
    }

    private static String mapParrotVariant(int variant) {
        if (variant < 0 || variant >= PARROT_VARIANT_NAMES.length) {
            return null;
        }

        return PARROT_VARIANT_NAMES[variant];
    }

    private static String formatVillagerLevel(int level) {
        return switch (level) {
            case 1 -> "Novice";
            case 2 -> "Apprentice";
            case 3 -> "Journeyman";
            case 4 -> "Expert";
            case 5 -> "Master";
            default -> Integer.toString(level);
        };
    }

    private static String formatHolderValue(net.minecraft.core.Holder<?> holder) {
        return holder.unwrapKey()
            .map(resourceKey -> formatResourceKey(resourceKey.toString()))
            .orElse(null);
    }

    private static String formatResourceKey(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        if (rawValue.startsWith("ResourceKey[") && rawValue.endsWith("]")) {
            int separator = rawValue.lastIndexOf(" / ");
            if (separator >= 0 && separator + 3 < rawValue.length() - 1) {
                return formatNameToken(rawValue.substring(separator + 3, rawValue.length() - 1));
            }
        }

        return formatNameToken(rawValue);
    }

    private static String getEnumLikeName(Object value) {
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        return value == null ? null : value.toString();
    }

    private static String formatNameToken(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue;
        int namespaceSeparator = normalized.indexOf(':');
        if (namespaceSeparator >= 0 && namespaceSeparator + 1 < normalized.length()) {
            normalized = normalized.substring(namespaceSeparator + 1);
        }

        int pathSeparator = normalized.lastIndexOf('/');
        if (pathSeparator >= 0 && pathSeparator + 1 < normalized.length()) {
            normalized = normalized.substring(pathSeparator + 1);
        }

        normalized = normalized.replace('-', '_').trim();
        if (normalized.isEmpty()) {
            return null;
        }

        String[] parts = normalized.toLowerCase(Locale.ROOT).split("_+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }

        return builder.isEmpty() ? null : builder.toString();
    }

    private static CompoundTag sanitizeEntityDataForSpawnEgg(CompoundTag entityTag) {
        CompoundTag sanitized = entityTag.copy();

        sanitized.remove("id");
        sanitized.remove("UUID");
        sanitized.remove("Pos");
        sanitized.remove("Motion");
        sanitized.remove("Rotation");
        sanitized.remove("SleepingX");
        sanitized.remove("SleepingY");
        sanitized.remove("SleepingZ");
        sanitized.remove("Leash");
        sanitized.remove("Health");
        sanitized.remove("DeathTime");
        sanitized.remove("HurtTime");
        sanitized.remove("HurtByTimestamp");
        sanitized.remove("FallDistance");
        sanitized.remove("Air");
        sanitized.remove("Fire");
        sanitized.remove("OnGround");
        sanitized.remove("Invulnerable");
        sanitized.remove("NoGravity");
        sanitized.remove("PortalCooldown");
        sanitized.remove("TicksFrozen");
        sanitized.remove("CanPickUpLoot");
        sanitized.remove("PersistenceRequired");
        sanitized.remove("LeftHanded");
        sanitized.remove("NoAI");
        sanitized.remove("Brain");
        sanitized.remove("Attributes");
        sanitized.remove("ArmorItems");
        sanitized.remove("HandItems");
        sanitized.remove("HandDropChances");
        sanitized.remove("ArmorDropChances");
        sanitized.remove("active_effects");
        sanitized.remove("effects");
        sanitized.remove("Passengers");
        sanitized.remove("Target");
        sanitized.remove("AngryAt");
        sanitized.remove("LoveCause");
        sanitized.remove("InLove");
        sanitized.remove("ForcedAge");
        sanitized.remove("Sitting");
        sanitized.remove("Owner");
        sanitized.remove("OwnerUUID");
        sanitized.remove("WanderTarget");
        sanitized.remove("PatrolTarget");
        sanitized.remove("Patrolling");
        sanitized.remove("Item");
        sanitized.remove("Inventory");
        sanitized.remove("Offers");
        sanitized.remove("Gossips");

        return sanitized;
    }
}
