package net.eggify.config;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {
    @TempDir
    Path directory;

    @Test
    void invalidStartupPreservesFileAndDoesNotInstallDefaults() throws Exception {
        Path file = directory.resolve("eggify.json");
        String invalid = "{\"useLuckPerms\": true, broken}";
        Files.writeString(file, invalid);
        ConfigManager manager = new ConfigManager(file);
        assertThrows(IllegalStateException.class, manager::load);
        assertEquals(invalid, Files.readString(file));
        assertThrows(IllegalStateException.class, manager::getConfig);
    }

    @Test
    void invalidReloadRetainsActivePermissionsAndBlacklist() throws Exception {
        Path file = directory.resolve("eggify.json");
        Files.writeString(file, "{\"useLuckPerms\":true,\"blacklistedMobs\":[\"minecraft:wither\"]}");
        ConfigManager manager = new ConfigManager(file);
        EggifyConfig active = manager.load();
        Files.writeString(file, "{broken");
        assertThrows(IllegalStateException.class, manager::load);
        assertSame(active, manager.getConfig());
        assertTrue(active.useLuckPerms);
        assertEquals("minecraft:wither", active.blacklistedMobs.getFirst());
        assertEquals("{broken", Files.readString(file));
    }

    @Test
    void emptyAndNullDocumentsAreRejected() throws Exception {
        Path file = directory.resolve("eggify.json");
        for (String invalid : new String[] {"", "null", "// comment only", "{} /* unclosed"}) {
            Files.writeString(file, invalid);
            assertThrows(IllegalStateException.class, () -> new ConfigManager(file).load());
            assertEquals(invalid, Files.readString(file));
        }
    }

    @Test
    void missingFileCreatesDefaultsAndValidLoadPreservesComments() throws Exception {
        Path file = directory.resolve("eggify.json");
        assertEquals(30.0, new ConfigManager(file).load().passiveDropChancePercent);
        assertTrue(Files.exists(file));
        String valid = "// Keep this comment\n{\"useLuckPerms\":true, /* note */ \"passiveDropChancePercent\":42}";
        Files.writeString(file, valid);
        EggifyConfig config = new ConfigManager(file).load();
        assertTrue(config.useLuckPerms);
        assertEquals(42.0, config.passiveDropChancePercent);
        assertEquals(valid, Files.readString(file));
    }
}
