package net.eggify.config;

import java.util.ArrayList;
import java.util.List;

public final class EggifyConfig {
    public Double dropChancePercent = null;
    public double passiveDropChancePercent = 30.0D;
    public double hostileDropChancePercent = 15.0D;
    public double bossDropChancePercent = 5.0D;
    public boolean useLuckPerms = false;
    public boolean allowCommandPermissionNode = false;
    public boolean allowDebugCommand = false;
    public boolean enableSpecialEgg = true;
    public double specialEggRecoveryChancePercent = 50.0D;
    public List<String> bossMobs = new ArrayList<>(List.of(
        "minecraft:ender_dragon",
        "minecraft:wither"
    ));
    public List<String> blacklistedMobs = new ArrayList<>();

    public void sanitize() {
        if (this.dropChancePercent != null && !Double.isNaN(this.dropChancePercent) && !Double.isInfinite(this.dropChancePercent)) {
            if (this.passiveDropChancePercent == 30.0D && this.hostileDropChancePercent == 15.0D && this.bossDropChancePercent == 5.0D) {
                double migratedChance = Math.clamp(this.dropChancePercent, 0.0D, 100.0D);
                this.passiveDropChancePercent = migratedChance;
                this.hostileDropChancePercent = migratedChance;
                this.bossDropChancePercent = migratedChance;
            }
        }

        this.dropChancePercent = null;
        this.passiveDropChancePercent = sanitizePercent(this.passiveDropChancePercent, 30.0D);
        this.hostileDropChancePercent = sanitizePercent(this.hostileDropChancePercent, 15.0D);
        this.bossDropChancePercent = sanitizePercent(this.bossDropChancePercent, 5.0D);
        this.specialEggRecoveryChancePercent = sanitizePercent(this.specialEggRecoveryChancePercent, 50.0D);
        this.bossMobs = sanitizeIdList(this.bossMobs);
        this.blacklistedMobs = sanitizeIdList(this.blacklistedMobs);
    }

    private static double sanitizePercent(double value, double fallback) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return fallback;
        }

        return Math.clamp(value, 0.0D, 100.0D);
    }

    private static List<String> sanitizeIdList(List<String> ids) {
        List<String> sanitized = ids == null ? new ArrayList<>() : new ArrayList<>(ids);
        sanitized.replaceAll(id -> id == null ? "" : id.trim().toLowerCase());
        sanitized.removeIf(String::isBlank);
        return sanitized;
    }
}
