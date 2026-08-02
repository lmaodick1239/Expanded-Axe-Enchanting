package com.smushytaco.expanded_axe_enchanting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModConfigTest {
    @TempDir
    Path tempDirectory;

    @Test
    void missingConfigUsesAndPersistsAllEnabledDefaults() throws IOException {
        Path path = tempDirectory.resolve("expanded_axe_enchanting.json");

        ModConfig config = ModConfig.createAndLoad(path);

        assertAllEnabled(config);
        assertAllEnabled(JsonParser.parseString(Files.readString(path)).getAsJsonObject());
    }

    @Test
    void loadsEveryPersistedToggle() throws IOException {
        Path path = tempDirectory.resolve("expanded_axe_enchanting.json");
        Files.writeString(path, allDisabledJson());

        ModConfig config = ModConfig.createAndLoad(path);

        assertAllDisabled(config);
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

    @ParameterizedTest
    @ValueSource(strings = {
            "{",
            "null",
            "[]",
            "{\"canUseFireAspectOnAxe\":\"yes\"}",
            "{\"canUseFireAspectOnAxe\":null}"
    })
    void invalidConfigIsBackedUpAndReplacedWithDefaults(String invalidJson) throws IOException {
        Path path = tempDirectory.resolve("expanded_axe_enchanting.json");
        Files.writeString(path, invalidJson);

        ModConfig config = ModConfig.createAndLoad(path);

        assertAllEnabled(config);
        assertTrue(Files.exists(path.resolveSibling(path.getFileName() + ".invalid")));
        assertTrue(Files.readString(path.resolveSibling(path.getFileName() + ".invalid")).equals(invalidJson));
        assertAllEnabled(JsonParser.parseString(Files.readString(path)).getAsJsonObject());
    }

    @Test
    void partialConfigKeepsDefaultsForMissingValues() throws IOException {
        Path path = tempDirectory.resolve("expanded_axe_enchanting.json");
        Files.writeString(path, "{\"canUseFireAspectOnAxe\":false}");

        ModConfig config = ModConfig.createAndLoad(path);

        assertFalse(config.getCanUseFireAspectOnAxe());
        assertTrue(config.getCanUseKnockbackOnAxe());
    }

    @Test
    void failedSaveDoesNotReplaceExistingTargetAndIsReported() throws IOException {
        Path path = tempDirectory.resolve("expanded_axe_enchanting.json");
        Files.createDirectory(path);
        ModConfig config = ModConfig.createAndLoad(tempDirectory.resolve("source.json"));

        assertThrows(IllegalStateException.class, () -> config.saveTo(path));
        assertTrue(Files.isDirectory(path));
    }

    private static String allDisabledJson() {
        return """
                {
                  "canUseFireAspectOnAxe": false,
                  "canUseKnockbackOnAxe": false,
                  "canUseLootingOnAxe": false,
                  "canUseImpalingOnAxe": false,
                  "canUseDensityOnAxe": false,
                  "canUseBreachOnAxe": false,
                  "canUseWindBurstOnAxe": false
                }
                """;
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

    private static void assertAllDisabled(ModConfig config) {
        assertFalse(config.getCanUseFireAspectOnAxe());
        assertFalse(config.getCanUseKnockbackOnAxe());
        assertFalse(config.getCanUseLootingOnAxe());
        assertFalse(config.getCanUseImpalingOnAxe());
        assertFalse(config.getCanUseDensityOnAxe());
        assertFalse(config.getCanUseBreachOnAxe());
        assertFalse(config.getCanUseWindBurstOnAxe());
    }

    private static void assertAllEnabled(JsonObject saved) {
        assertTrue(saved.get("canUseFireAspectOnAxe").getAsBoolean());
        assertTrue(saved.get("canUseKnockbackOnAxe").getAsBoolean());
        assertTrue(saved.get("canUseLootingOnAxe").getAsBoolean());
        assertTrue(saved.get("canUseImpalingOnAxe").getAsBoolean());
        assertTrue(saved.get("canUseDensityOnAxe").getAsBoolean());
        assertTrue(saved.get("canUseBreachOnAxe").getAsBoolean());
        assertTrue(saved.get("canUseWindBurstOnAxe").getAsBoolean());
    }
}
