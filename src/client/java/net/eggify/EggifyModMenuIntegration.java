package net.eggify;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EggifyModMenuIntegration implements ModMenuApi {
    private static boolean warnedConfigScreenUnavailable;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return EggifyModMenuIntegration::createConfigScreen;
    }

    static boolean hasCompatibleClothConfig() {
        var cloth = FabricLoader.getInstance().getModContainer("cloth-config");
        if (cloth.isEmpty()) {
            return false;
        }
        try {
            return VersionPredicate.parse(">=26.4-").test(cloth.get().getMetadata().getVersion());
        } catch (VersionParsingException exception) {
            return false;
        }
    }

    private static Screen createConfigScreen(Screen parent) {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
            return notice(parent, "Install Cloth Config for Minecraft 26.4 to open this menu. "
                + "You can also edit config/eggify.json and run /eggify reload.");
        }
        if (!hasCompatibleClothConfig()) {
            return notice(parent, "The installed Cloth Config version is too old for Minecraft 26.4. "
                + "Install a compatible version to open this menu. Until then, edit config/eggify.json "
                + "and run /eggify reload.");
        }
        try {
            return EggifyClothConfigScreen.create(parent);
        } catch (LinkageError exception) {
            if (!warnedConfigScreenUnavailable) {
                warnedConfigScreenUnavailable = true;
                EggifyMod.LOGGER.warn("{} Cloth Config is installed but incompatible with this Minecraft version; the ModMenu config screen is disabled.", EggifyMod.LOG_PREFIX, exception);
            }
            return notice(parent, "Cloth Config could not open this menu because it is incompatible. "
                + "Install a compatible version for Minecraft 26.4, or edit config/eggify.json "
                + "and run /eggify reload.");
        }
    }

    private static Screen notice(Screen parent, String message) {
        return new AlertScreen(() -> Minecraft.getInstance().setScreenAndShow(parent),
            Component.literal("Eggify Config"), Component.literal(message));
    }
}
