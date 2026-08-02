package com.smushytaco.expanded_axe_enchanting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModMenuIntegrationTest {
    @Test
    void toggleModelMapsAllSevenConfigValuesInScreenOrder() {
        ModConfig config = ModConfig.createAndLoad(Path.of("build/test-modmenu-config.json"));

        List<ConfigToggle> toggles = ConfigToggle.all();

        assertEquals(List.of(
                "canUseFireAspectOnAxe",
                "canUseKnockbackOnAxe",
                "canUseLootingOnAxe",
                "canUseImpalingOnAxe",
                "canUseDensityOnAxe",
                "canUseBreachOnAxe",
                "canUseWindBurstOnAxe"
        ), toggles.stream().map(ConfigToggle::key).toList());
        toggles.forEach(toggle -> toggle.set(config, false));
        assertTrue(toggles.stream().noneMatch(toggle -> toggle.get(config)));
    }

    @Test
    void metadataRegistersRepositoryOwnedModMenuEntrypoint() throws IOException {
        JsonObject metadata = JsonParser.parseString(Files.readString(Path.of("src/main/resources/fabric.mod.json")))
                .getAsJsonObject();

        assertEquals(
                "com.smushytaco.expanded_axe_enchanting.ExpandedAxeEnchantingModMenu",
                metadata.getAsJsonObject("entrypoints").getAsJsonArray("modmenu").get(0).getAsString()
        );
        assertTrue(ModMenuApi.class.isAssignableFrom(ExpandedAxeEnchantingModMenu.class));
    }
}
