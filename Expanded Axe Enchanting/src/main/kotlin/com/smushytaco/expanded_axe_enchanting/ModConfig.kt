package com.smushytaco.expanded_axe_enchanting

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path

class ModConfig private constructor(@Transient private val path: Path) {
    var canUseFireAspectOnAxe = true
    var canUseKnockbackOnAxe = true
    var canUseLootingOnAxe = true
    var canUseImpalingOnAxe = true
    var canUseDensityOnAxe = true
    var canUseBreachOnAxe = true
    var canUseWindBurstOnAxe = true

    fun save() {
        Files.createDirectories(path.parent)
        Files.writeString(path, GSON.toJson(this))
    }

    private fun load() {
        if (!Files.exists(path)) {
            save()
            return
        }

        val json = Files.newBufferedReader(path).use(JsonParser::parseReader).asJsonObject
        canUseFireAspectOnAxe = json.booleanOrDefault("canUseFireAspectOnAxe")
        canUseKnockbackOnAxe = json.booleanOrDefault("canUseKnockbackOnAxe")
        canUseLootingOnAxe = json.booleanOrDefault("canUseLootingOnAxe")
        canUseImpalingOnAxe = json.booleanOrDefault("canUseImpalingOnAxe")
        canUseDensityOnAxe = json.booleanOrDefault("canUseDensityOnAxe")
        canUseBreachOnAxe = json.booleanOrDefault("canUseBreachOnAxe")
        canUseWindBurstOnAxe = json.booleanOrDefault("canUseWindBurstOnAxe")
    }

    companion object {
        private val GSON = GsonBuilder().setPrettyPrinting().create()

        @JvmStatic
        fun createAndLoad(): ModConfig = createAndLoad(
            FabricLoader.getInstance().configDir.resolve("${ExpandedAxeEnchanting.MOD_ID}.json")
        )

        @JvmStatic
        fun createAndLoad(path: Path): ModConfig = ModConfig(path).apply { load() }
    }
}

private fun com.google.gson.JsonObject.booleanOrDefault(name: String): Boolean =
    if (has(name)) get(name).asBoolean else true
