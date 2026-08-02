package com.smushytaco.expanded_axe_enchanting

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

class ModConfig private constructor(@Transient private val path: Path) {
    var canUseFireAspectOnAxe = true
    var canUseKnockbackOnAxe = true
    var canUseLootingOnAxe = true
    var canUseImpalingOnAxe = true
    var canUseDensityOnAxe = true
    var canUseBreachOnAxe = true
    var canUseWindBurstOnAxe = true

    fun save() = saveTo(path)

    fun saveTo(target: Path) {
        val parent = target.toAbsolutePath().parent
        var temporary: Path? = null
        try {
            Files.createDirectories(parent)
            temporary = Files.createTempFile(parent, target.fileName.toString(), ".tmp")
            val bytes = GSON.toJson(this).toByteArray(StandardCharsets.UTF_8)
            FileChannel.open(temporary, StandardOpenOption.WRITE).use { channel ->
                val buffer = ByteBuffer.wrap(bytes)
                while (buffer.hasRemaining()) channel.write(buffer)
                channel.force(true)
            }
            try {
                Files.move(
                    temporary,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
            }
            temporary = null
        } catch (exception: IOException) {
            throw IllegalStateException("Unable to save configuration to $target", exception)
        } finally {
            temporary?.let { Files.deleteIfExists(it) }
        }
    }

    private fun load() {
        if (!Files.exists(path)) {
            save()
            return
        }

        try {
            val json = Files.newBufferedReader(path).use(JsonParser::parseReader).asJsonObject
            apply(json)
        } catch (exception: IOException) {
            throw IllegalStateException("Unable to read configuration from $path", exception)
        } catch (exception: RuntimeException) {
            if (exception !is JsonParseException && exception !is IllegalStateException) throw exception
            recoverInvalidConfig(exception)
        }
    }

    private fun apply(json: JsonObject) {
        canUseFireAspectOnAxe = json.booleanOrDefault("canUseFireAspectOnAxe")
        canUseKnockbackOnAxe = json.booleanOrDefault("canUseKnockbackOnAxe")
        canUseLootingOnAxe = json.booleanOrDefault("canUseLootingOnAxe")
        canUseImpalingOnAxe = json.booleanOrDefault("canUseImpalingOnAxe")
        canUseDensityOnAxe = json.booleanOrDefault("canUseDensityOnAxe")
        canUseBreachOnAxe = json.booleanOrDefault("canUseBreachOnAxe")
        canUseWindBurstOnAxe = json.booleanOrDefault("canUseWindBurstOnAxe")
    }

    private fun recoverInvalidConfig(cause: RuntimeException) {
        val backup = nextInvalidBackup()
        try {
            Files.move(path, backup)
        } catch (exception: IOException) {
            throw IllegalStateException("Invalid configuration at $path could not be preserved", exception)
        }
        System.err.println("Invalid configuration at $path was moved to $backup: ${cause.message}")
        save()
    }

    private fun nextInvalidBackup(): Path {
        val base = path.resolveSibling("${path.fileName}.invalid")
        if (!Files.exists(base)) return base
        var suffix = 1
        while (Files.exists(path.resolveSibling("${path.fileName}.invalid.$suffix"))) suffix++
        return path.resolveSibling("${path.fileName}.invalid.$suffix")
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

private fun JsonObject.booleanOrDefault(name: String): Boolean {
    if (!has(name)) return true
    val value = get(name)
    if (!value.isJsonPrimitive || !value.asJsonPrimitive.isBoolean) {
        throw IllegalStateException("Configuration value '$name' must be a boolean")
    }
    return value.asBoolean
}
