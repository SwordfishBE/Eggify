package net.eggify;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;

public final class UpdateChecker {
    private static final String PROJECT_ID = "kOkcRRPb";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    private UpdateChecker() {
    }

    public static void checkForUpdates() {
        if (!EggifyMod.CONFIG.getConfig().enableUpdateCheck) {
            return;
        }

        String currentVersion = FabricLoader.getInstance().getModContainer(EggifyMod.MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
        String loaders = URLEncoder.encode("[\"fabric\"]", StandardCharsets.UTF_8);
        String versions = URLEncoder.encode("[\"" + EggifyMod.MINECRAFT_VERSION + "\"]", StandardCharsets.UTF_8);
        URI uri = URI.create("https://api.modrinth.com/v2/project/" + PROJECT_ID + "/version?loaders=" + loaders + "&game_versions=" + versions);
        HttpRequest request = HttpRequest.newBuilder(uri)
            .header("User-Agent", "Eggify/" + currentVersion + " (modrinth update check)")
            .timeout(Duration.ofSeconds(8))
            .GET()
            .build();

        CompletableFuture.runAsync(() -> {
            try {
                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    EggifyMod.LOGGER.debug("{} Modrinth update check returned status {}", EggifyMod.LOG_PREFIX, response.statusCode());
                    return;
                }

                JsonElement parsed = JsonParser.parseString(response.body());
                if (!parsed.isJsonArray()) {
                    return;
                }

                JsonArray versionsArray = parsed.getAsJsonArray();
                if (versionsArray.isEmpty()) {
                    return;
                }

                String latestVersion = versionsArray.get(0).getAsJsonObject().get("version_number").getAsString();
                if (!currentVersion.equals(latestVersion)) {
                    EggifyMod.LOGGER.info("{} Update available on Modrinth: current={}, latest={}", EggifyMod.LOG_PREFIX, currentVersion, latestVersion);
                }
            } catch (IOException | InterruptedException exception) {
                EggifyMod.LOGGER.debug("{} Update check failed: {}", EggifyMod.LOG_PREFIX, exception.getMessage());
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
