# Minecraft 26.2 Migration Report & Status

This document details the migration to **Minecraft 26.2 (Java 25)** for **Minecraft Server Properties Reload**.

---

## ✅ MAPPINGS BLOCKER RESOLVED

**Previous (incorrect) diagnosis**: The migration was believed to be blocked by "missing Mojang mappings".

**Actual root cause**: Minecraft 26.1+ ships **unobfuscated**, so Fabric Loom must run in its
non-remapping mode where **no `mappings` configuration is needed at all**. The old build script
still declared `mappings(loom.layered { officialMojangMappings() })`, which forced Loom down the
obfuscated/remap code path and failed.

### The fix (verified working)

1. **Remove the `mappings` block entirely** from `Fabric/build.gradle.kts` — 26.1+ needs no mappings.
2. **Use the non-remap Loom plugin** `net.fabricmc.fabric-loom` at version `1.17-SNAPSHOT`.
3. **Set `fabric.loom.disableObfuscation=true`** in `gradle.properties`.

Why step 3 is required here: Loom auto-detects the non-obfuscated (`net.fabricmc.fabric-loom`)
plugin and sets `disableObfuscation=true` automatically **only for single-project builds**. In this
multi-project Kotlin build, Loom's extension is constructed before the plugin registration is
observable, so it defaults to the obfuscated path and throws:

```
Failed to setup Minecraft, java.lang.IllegalArgumentException: Configuration 'mappings' has no dependencies
```

Setting `fabric.loom.disableObfuscation=true` explicitly forces the non-remap path (confirmed in
Loom source `LoomGradleExtensionImpl` — the flag gates whether the `mappings` configuration is
resolved). After this change, Loom compiles directly against the unobfuscated 26.2 jars.

Reference: the working single-project example at `../high-level-enchants` builds cleanly with no
mappings block, confirming the approach.

**Toolchain**:
- ✅ Gradle 9.5.0
- ✅ Loom 1.17-SNAPSHOT (resolves to 1.17.17)
- ✅ Java 25
- ✅ Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.152.1+26.2
- ✅ No mappings required (unobfuscated game)

---

## ⚠️ REMAINING WORK: DedicatedServerProperties API changed substantially

With the build unblocked, the compiler now reports the real 26.2 API differences. The migration
doc's earlier assumption (that every field just needs a `.get()` wrapper) is **incorrect**. In 26.2:

### Renamed fields (still `Settings.MutableValue<T>`, still need `.get()`)
| 1.18 field | 26.2 field | Type |
| :--- | :--- | :--- |
| `gamemode` | `gameMode` | `GameType` |
| `opPermissionLevel` | `opPermissions` | `LevelBasedPermissionSet` |
| `functionPermissionLevel` | `functionPermissions` | `LevelBasedPermissionSet` (direct field, **not** a `MutableValue`) |

### Fields REMOVED from `DedicatedServerProperties` in 26.2
These no longer exist and cannot be mapped 1:1. They were removed from `server.properties` in
vanilla Minecraft and now live elsewhere (world options / game rules) or were dropped:
- `allowNether`
- `enableCommandBlock`
- `pvp`
- `spawnAnimals`
- `spawnMonsters`
- `spawnNpcs`
- `resourcePack`
- `resourcePackSha1` (resource pack info is now a single `Optional<ServerResourcePackInfo> serverResourcePackInfo`)

**Decision required**: The property classes for the removed items
(`AllowNetherProperty`, `EnableCommandBlockProperty`, `PvpProperty`, `SpawnAnimalsProperty`,
`SpawnMonstersProperty`, `SpawnNpcsProperty`, `ResourcePackProperty`, `ResourcePackSha1Property`)
must either be dropped from the supported set or re-implemented against the new API surface
(e.g. world options / game rules), which is a product decision, not a mechanical rename.

Total remaining `compileJava` errors: **22** across **11** files (listed above).

---

## 1. Stack & Toolchain Upgrades (Attempted)

| Dependency / Tool | Legacy (1.18.2) | Target (26.2) | Status |
| :--- | :--- | :--- | :--- |
| **Java SDK** | Java 17 | **Java 25** | ✅ Configured |
| **Gradle Wrapper** | 7.2 | **9.5.0** | ✅ Upgraded |
| **Fabric Loom** | `0.10-SNAPSHOT` | `1.17.0-alpha.7` | ⚠️ Plugin resolves but mappings fail |
| **Fabric Loader** | `0.12.8` | `0.19.3` | ✅ Configured |
| **Fabric API** | `0.46.2+1.18` | `0.152.1+26.2` | ✅ Configured |
| **Forge Gradle** | `5.1.+` | `6.0.+` | ⏸️ Not tested (blocked by Fabric) |
| **Mappings** | Yarn / Sponge Vanilla | **Mojang Mappings** | ❌ **BLOCKER: Not available via Loom** |

---

## 2. Code Changes & API Patches Required

### A. DedicatedServerProperties Accessors (`Settings.MutableValue<T>`)
In 26.2, fields on `DedicatedServerProperties` are wrapped in `Settings.MutableValue<T>` objects:
- **Change**: Replace direct field access `properties.allowFlight` with `properties.allowFlight.get()`.
- **Affected Properties**:
  - `AllowFlightProperty` (`allowFlight.get()`)
  - `AllowNetherProperty` (`allowNether.get()`)
  - `BroadcastConsoleToOpsProperty` (`broadcastConsoleToOps.get()`)
  - `BroadcastRconToOpsProperty` (`broadcastRconToOps.get()`)
  - `DifficultyProperty` (`difficulty.get()`)
  - `EnableCommandBlockProperty` (`enableCommandBlock.get()`)
  - `EnableJmxMonitoringProperty` (`enableJmxMonitoring.get()`)
  - `EnableQueryProperty` (`enableQuery.get()`)
  - `EnableRconProperty` (`enableRcon.get()`)
  - `EnableStatusProperty` (`enableStatus.get()`)
  - `EnforceWhitelistProperty` (`enforceWhitelist.get()`)
  - `EntityBroadcastRangePercentageProperty` (`entityBroadcastRangePercentage.get()`)
  - `ForceGamemodeProperty` (`forceGameMode.get()`)
  - `FunctionPermissionLevel` (`functionPermissionLevel.get()`)
  - `HardcoreProperty` (`hardcore.get()`)
  - `HideOnlinePlayersProperty` (`hideOnlinePlayers.get()`)
  - `MaxPlayersProperty` (`maxPlayers.get()`)
  - `MotdProperty` (`motd.get()`)
  - `OpPermissionLevel` (`opPermissionLevel.get()`)
  - `PvpProperty` (`pvp.get()`)
  - `QueryPortProperty` (`queryPort.get()`)
  - `RconPasswordProperty` (`rconPassword.get()`)
  - `SimulationDistanceProperty` (`simulationDistance.get()`)
  - `SpawnProtectionProperty` (`spawnProtection.get()`)
  - `ViewDistanceProperty` (`viewDistance.get()`)

### B. Chat Component API (`Component.literal`)
- `TextComponent` has been removed from Minecraft 26.2.
- Replace `new TextComponent("...")` with `Component.literal("...")`.
- Updated `s.sendSuccess(...)` calls in `PropertiesCommand` to pass a `Supplier<Component>` lambda `() -> Component.literal(...)`.

### C. DedicatedServer & Server API Changes
- **WhiteList / Player List**: `server.getPlayerList().setUsingWhiteList(...)` changed to `server.getPlayerList().setUsingWhiteList(...)` / updated whitelist handling.
- **Whitelist Kicking**: `server.kickUnlistedPlayers(...)` parameter signature changed to parameterless `server.kickUnlistedPlayers()`.
- **Resource Pack APIs**: DedicatedServer resource pack handling refactored to single pack info object in 26.2.

---

## 3. Recommended Build & Verification Steps

1. **Gradle Build**:
   ```bash
   JAVA_HOME="/path/to/jdk-25" ./gradlew build
   ```
2. **Subproject Validation**:
   - Fabric: `./gradlew :Fabric:build`
   - Forge (if enabled): `./gradlew :Forge:build`
3. **Git Push Dry Run**:
   ```bash
   git push --dry-run origin main:main
   ```
