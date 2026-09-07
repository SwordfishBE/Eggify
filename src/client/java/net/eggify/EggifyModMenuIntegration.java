package net.eggify;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

public final class EggifyModMenuIntegration implements ModMenuApi {
    private static boolean warnedConfigScreenUnavailable;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
            return parent -> null;
        }

        return EggifyModMenuIntegration::createConfigScreen;
    }

    private static Screen createConfigScreen(Screen parent) {
        try {
            return EggifyClothConfigScreen.create(parent);
        } catch (LinkageError exception) {
            if (!warnedConfigScreenUnavailable) {
                warnedConfigScreenUnavailable = true;
                EggifyMod.LOGGER.warn("{} Cloth Config is installed but incompatible with this Minecraft version; the ModMenu config screen is disabled.", EggifyMod.LOG_PREFIX, exception);
            }
            return null;
        }
    }
}
