package com.smushytaco.expanded_axe_enchanting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModMenuIntegrationTest {
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
