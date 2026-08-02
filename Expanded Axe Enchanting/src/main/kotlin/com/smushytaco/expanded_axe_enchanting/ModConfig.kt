package com.smushytaco.expanded_axe_enchanting

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import java.io.StringReader
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
            if (migrateLegacy()) return
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

    private fun migrateLegacy(): Boolean {
        val legacy = path.resolveSibling("${path.fileName.toString().removeSuffix(".json")}.json5")
        if (!Files.exists(legacy)) return false

        try {
            val content = Files.readString(legacy)
            val normalized = content
                .replace(Regex("(?s)/\\*.*?\\*/|//[^\\r\\n]*"), "")
                .replace(Regex(",\\s*([}\\]])"), "$1")
            val json = JsonReader(StringReader(normalized)).apply { isLenient = true }
                .use(JsonParser::parseReader).asJsonObject
            apply(json)
        } catch (exception: IOException) {
            throw IllegalStateException("Unable to read legacy configuration from $legacy", exception)
        } catch (exception: RuntimeException) {
            if (exception !is JsonParseException && exception !is IllegalStateException) throw exception
            System.err.println("Invalid legacy configuration at $legacy was preserved; enabled defaults will be used: ${exception.message}")
        }
        save()
        return true
    }

    private fun apply(json: JsonObject) {
        val fireAspect = json.booleanOrDefault("canUseFireAspectOnAxe")
        val knockback = json.booleanOrDefault("canUseKnockbackOnAxe")
        val looting = json.booleanOrDefault("canUseLootingOnAxe")
        val impaling = json.booleanOrDefault("canUseImpalingOnAxe")
        val density = json.booleanOrDefault("canUseDensityOnAxe")
        val breach = json.booleanOrDefault("canUseBreachOnAxe")
        val windBurst = json.booleanOrDefault("canUseWindBurstOnAxe")

        canUseFireAspectOnAxe = fireAspect
        canUseKnockbackOnAxe = knockback
        canUseLootingOnAxe = looting
        canUseImpalingOnAxe = impaling
        canUseDensityOnAxe = density
        canUseBreachOnAxe = breach
        canUseWindBurstOnAxe = windBurst
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
