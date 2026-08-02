package com.smushytaco.expanded_axe_enchanting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModConfigTest {
    @TempDir
    Path tempDirectory;

    @Test
    void missingConfigUsesAndPersistsAllEnabledDefaults() throws IOException {
        Path path = tempDirectory.resolve("expanded_axe_enchanting.json");

        ModConfig config = ModConfig.createAndLoad(path);

        assertAllEnabled(config);
        JsonObject saved = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        assertTrue(saved.get("canUseFireAspectOnAxe").getAsBoolean());
        assertTrue(saved.get("canUseKnockbackOnAxe").getAsBoolean());
        assertTrue(saved.get("canUseLootingOnAxe").getAsBoolean());
        assertTrue(saved.get("canUseImpalingOnAxe").getAsBoolean());
        assertTrue(saved.get("canUseDensityOnAxe").getAsBoolean());
        assertTrue(saved.get("canUseBreachOnAxe").getAsBoolean());
        assertTrue(saved.get("canUseWindBurstOnAxe").getAsBoolean());
    }

    @Test
    void loadsEveryPersistedToggle() throws IOException {
        Path path = tempDirectory.resolve("expanded_axe_enchanting.json");
        Files.writeString(path, """
                {
                  "canUseFireAspectOnAxe": false,
                  "canUseKnockbackOnAxe": false,
                  "canUseLootingOnAxe": false,
                  "canUseImpalingOnAxe": false,
                  "canUseDensityOnAxe": false,
                  "canUseBreachOnAxe": false,
                  "canUseWindBurstOnAxe": false
                }
                """);

        ModConfig config = ModConfig.createAndLoad(path);

        assertFalse(config.getCanUseFireAspectOnAxe());
        assertFalse(config.getCanUseKnockbackOnAxe());
        assertFalse(config.getCanUseLootingOnAxe());
        assertFalse(config.getCanUseImpalingOnAxe());
        assertFalse(config.getCanUseDensityOnAxe());
        assertFalse(config.getCanUseBreachOnAxe());
        assertFalse(config.getCanUseWindBurstOnAxe());
    }

    @Test
    void savesChangedValuesForReload() {
        Path path = tempDirectory.resolve("expanded_axe_enchanting.json");
        ModConfig config = ModConfig.createAndLoad(path);
        config.setCanUseLootingOnAxe(false);
        config.setCanUseWindBurstOnAxe(false);

        config.save();
        ModConfig reloaded = ModConfig.createAndLoad(path);

        assertFalse(reloaded.getCanUseLootingOnAxe());
        assertFalse(reloaded.getCanUseWindBurstOnAxe());
        assertTrue(reloaded.getCanUseFireAspectOnAxe());
    }

    private static void assertAllEnabled(ModConfig config) {
        assertTrue(config.getCanUseFireAspectOnAxe());
        assertTrue(config.getCanUseKnockbackOnAxe());
        assertTrue(config.getCanUseLootingOnAxe());
        assertTrue(config.getCanUseImpalingOnAxe());
        assertTrue(config.getCanUseDensityOnAxe());
        assertTrue(config.getCanUseBreachOnAxe());
        assertTrue(config.getCanUseWindBurstOnAxe());
    }
}
