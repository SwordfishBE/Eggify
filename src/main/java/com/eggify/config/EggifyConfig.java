package com.eggify.config;

import java.util.ArrayList;
import java.util.List;

public final class EggifyConfig {
    public double dropChancePercent = 2.5D;
    public boolean useLuckPerms = false;
    public boolean allowCommandPermissionNode = false;
    public boolean enableUpdateCheck = true;
    public List<String> blacklistedMobs = new ArrayList<>(List.of(
        "minecraft:ender_dragon",
        "minecraft:wither"
    ));

    public void sanitize() {
        if (Double.isNaN(this.dropChancePercent) || Double.isInfinite(this.dropChancePercent)) {
            this.dropChancePercent = 2.5D;
        }

        this.dropChancePercent = Math.clamp(this.dropChancePercent, 0.0D, 100.0D);
        this.blacklistedMobs = this.blacklistedMobs == null ? new ArrayList<>() : new ArrayList<>(this.blacklistedMobs);
        this.blacklistedMobs.replaceAll(id -> id == null ? "" : id.trim().toLowerCase());
        this.blacklistedMobs.removeIf(String::isBlank);
    }
}
