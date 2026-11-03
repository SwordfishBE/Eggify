package net.eggify;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.TitleScreen;

public class ConfigNoticeClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        boolean expectedCloth = Boolean.parseBoolean(System.getProperty("eggify.test.expectCloth"));
        if (FabricLoader.getInstance().isModLoaded("cloth-config") != expectedCloth) {
            throw new AssertionError("The client must run with the requested Cloth Config presence");
        }
        context.runOnClient(client -> {
            if (EggifyModMenuIntegration.hasCompatibleClothConfig()) {
                throw new AssertionError("This test requires missing or older Cloth Config");
            }
            var parent = new TitleScreen();
            var screen = new EggifyModMenuIntegration().getModConfigScreenFactory().create(parent);
            if (!(screen instanceof AlertScreen)) {
                throw new AssertionError("Missing or older Cloth Config must show a notice");
            }
            client.setScreenAndShow(screen);
        });
        context.waitTicks(3);
        context.takeScreenshot(expectedCloth ? "eggify-old-cloth-notice" : "eggify-missing-cloth-notice");
        context.clickScreenButton("Back");
        context.waitForScreen(TitleScreen.class);
    }
}
