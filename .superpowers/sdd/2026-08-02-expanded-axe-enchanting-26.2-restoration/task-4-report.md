# Task 4: Runtime and Regression Verification Report

## Status

Partial runtime verification with one focused correction committed:

- Clean build and final test/build: **PASS**
- Dedicated server startup: **PASS** after correction; reached `Done (2.416s)!` and was stopped
- Client startup: **FAIL (upstream dependency incompatibility)** before main menu; owo 0.13.0+26.1 has a Minecraft 26.2-invalid client mixin
- Project diagnostics: **PASS** (no diagnostics)
- Repository/diff hygiene: **PASS**; only unrelated `MIGRATION_26.2.md` remains untracked

## Baseline

- Requested implementation commit: `8f3c62d fix: restore axe enchantment applicability`
- Initial branch: `main...origin/main [ahead 5]`
- Initial worktree: only `?? MIGRATION_26.2.md`
- Gradle project directory: `/mnt/Random/users/user/Documents/GitHub/Expanded-Axe-Enchanting/Expanded Axe Enchanting`

## Commands and Evidence

### 1. Clean distributable build

```bash
./gradlew clean build --no-daemon
```

Result: exit 0, `BUILD SUCCESSFUL in 23s`, 11 actionable tasks (10 executed, 1 up-to-date).

Artifacts confirmed:

- `build/libs/expanded-axe-enchanting-1.1.11.jar`
- `build/libs/expanded-axe-enchanting-1.1.11-sources.jar`

Warnings were limited to the existing deprecated test API notice, JOML's use of a terminally deprecated `sun.misc.Unsafe` method, and Trae sandbox `/proc/.../coredump_filter` restrictions after Gradle had already completed successfully.

### 2. Dedicated server smoke and defect reproduction

Accepted the EULA in ignored runtime path `run/eula.txt`, then ran:

```bash
./gradlew runServer --no-daemon
```

Initial result: Gradle itself exited successfully, but Minecraft logged a decisive initialization failure:

```text
Failed to start the minecraft server
Caused by: java.lang.NoSuchMethodError: 'boolean net.minecraft.ChatFormatting.isColor()'
    at io.wispforest.owo.ui.core.Color.<clinit>(Color.java:29)
    at io.wispforest.owo.config.ConfigWrapper.<init>(ConfigWrapper.java:90)
    at com.smushytaco.expanded_axe_enchanting.ModConfig.<init>(ModConfig.kt:8)
```

Scientific diagnosis:

1. Reproduced consistently through the ordinary Loom server run.
2. `javap` confirmed Minecraft 26.1.2 provides `ChatFormatting.isColor()`, `getColor()`, and `getName()`.
3. `javap` confirmed Minecraft 26.2 removes all three.
4. `javap -c` confirmed owo 0.13.0+26.1 directly invokes these methods while statically initializing its `Color` class.
5. Upstream Maven metadata contains no released owo 26.2 artifact; upstream's 26.2 work is still a draft pull request. The draft explicitly replaces the removed formatting metadata with a hardcoded lookup.

### 3. Test-first correction

Added `ChatFormattingCompatibilityTest` first and ran:

```bash
./gradlew test --tests com.smushytaco.expanded_axe_enchanting.ChatFormattingCompatibilityTest --no-daemon
```

Red result: expected compilation failure because `ChatFormattingCompatibility` did not exist (six `cannot find symbol` errors).

Applied the minimum server-enabling compatibility correction:

- Added a tested legacy color/name lookup for Minecraft's 16 color formatting constants.
- Added a mixin that restores owo's expected `isColor()`, `getColor()`, and `getName()` methods to `ChatFormatting`.
- Registered the mixin.

Green rerun:

```bash
./gradlew test --tests com.smushytaco.expanded_axe_enchanting.ChatFormattingCompatibilityTest --no-daemon
```

Result: exit 0, `BUILD SUCCESSFUL in 12s`.

Correction commit:

```text
2882507 fix: bridge owo formatting on 26.2
```

Files in the commit:

- `/mnt/Random/users/user/Documents/GitHub/Expanded-Axe-Enchanting/Expanded Axe Enchanting/src/main/java/com/smushytaco/expanded_axe_enchanting/ChatFormattingCompatibility.java`
- `/mnt/Random/users/user/Documents/GitHub/Expanded-Axe-Enchanting/Expanded Axe Enchanting/src/main/java/com/smushytaco/expanded_axe_enchanting/mixins/ChatFormattingCompatibilityMixin.java`
- `/mnt/Random/users/user/Documents/GitHub/Expanded-Axe-Enchanting/Expanded Axe Enchanting/src/main/resources/expanded_axe_enchanting.mixins.json`
- `/mnt/Random/users/user/Documents/GitHub/Expanded-Axe-Enchanting/Expanded Axe Enchanting/src/test/java/com/smushytaco/expanded_axe_enchanting/ChatFormattingCompatibilityTest.java`

The Git post-commit hook warned that `git-lfs` was not installed, but the commit completed successfully.

### 4. Dedicated server verification after correction

```bash
./gradlew runServer --no-daemon
```

Decisive successful startup evidence:

```text
Starting minecraft server version 26.2
Starting Minecraft server on *:25565
Preparing spawn area: 100%
Done (2.416s)! For help, type "help"
```

No `InvalidInjectionException`, project mixin missing-target error, registry lifecycle exception, mod initialization failure, or `NoSuchMethodError` occurred. The server also immediately saved all dimensions. The Gradle/server process was then explicitly stopped. A later process check found no `Main`, `KnotClient`, or `KnotServer` runtime.

Observed owo warnings about client-only/missing optional targets on the logical server (`ClickableStyleFinder`, `RenderType`, `LanguageReader`, `CommandEncoder`) did not prevent startup and were not from this mod's mixin. The first-run missing `server.properties` message was followed by normal default-property generation and successful startup.

### 5. Client smoke test

Environment had `DISPLAY` set. No Prism account file was auto-discovered and `runClientAuth` was not registered, so no credentials or tokens were used. Ran ordinary offline launch:

```bash
./gradlew runClient --no-daemon
```

Result: failed before main-menu evidence with an owo dependency mixin incompatibility:

```text
Mixin apply for mod owo failed owo.mixins.json:ui.MinecraftMixin
InvalidMixinException: @Shadow field screen was not located in the target class net.minecraft.client.Minecraft. No refMap loaded.
```

This is consistent with Minecraft 26.2 moving screen access from `Minecraft.screen` to `Minecraft.gui.screen()`, a change already addressed only in upstream owo's unreleased draft 26.2 port. Fixing this locally would require patching or vendoring a broad third-party client library migration (the draft changes dozens of files), which is not a minimal repository correction. No further speculative compatibility shims were added. The client process exited with Gradle failure and was not left running.

The decisive client log contained no failure from `expanded_axe_enchanting.mixins.AxeModification`; the blocker occurs while applying owo's `ui.MinecraftMixin` to `net.minecraft.client.Minecraft`.

### 6. Diagnostics and final verification

IDE diagnostics returned an empty set.

```bash
./gradlew test build --no-daemon
```

Result: exit 0, `BUILD SUCCESSFUL in 12s`, 10 actionable tasks (2 executed, 8 up-to-date).

```bash
git status --short --branch
git diff HEAD~2..HEAD --check
git log --oneline -5
git show --stat --oneline HEAD
```

Results:

```text
## main...origin/main [ahead 6]
?? MIGRATION_26.2.md
```

`git diff HEAD~2..HEAD --check` produced no output (no whitespace errors).

Recent commits:

```text
2882507 fix: bridge owo formatting on 26.2
8f3c62d fix: restore axe enchantment applicability
5e009ca build: update Gradle wrapper for Loom 1.17
400823f build: migrate to Minecraft 26.2
d0ec58e docs: add 26.2 restoration plan
```

Runtime artifacts accidentally emitted at the nested project root were deleted. The standard `run/` runtime directory remains ignored. `MIGRATION_26.2.md` was never modified or staged.

## Concerns

1. **Client verification remains blocked by owo 0.13.0+26.1.** There is no released Fabric owo artifact for Minecraft 26.2 in the configured upstream repository as of this verification. Upstream's draft 26.2 port addresses the observed `Minecraft.screen` migration but is explicitly draft/unreleased and includes broad API changes and known issues.
2. **Dedicated-server startup is verified**, including this mod's initialization and required mixins, but client main-menu startup cannot be claimed.
3. The focused local compatibility bridge restores only the three removed `ChatFormatting` methods needed to pass the first owo/server initialization defect; it intentionally does not attempt to reproduce owo's entire upstream 26.2 migration.

## References

- Upstream owo releases: https://github.com/wisp-forest/owo-lib/releases
- Upstream draft Minecraft 26.2 port: https://github.com/wisp-forest/owo-lib/pull/490
- Upstream Maven metadata: https://maven.wispforest.io/releases/io/wispforest/owo-lib/maven-metadata.xml

---

## Fix Round 1

### Status

The load-bearing owo incompatibility is removed. The mod now owns its small JSON configuration implementation and no longer ships or declares owo/KSP. Dedicated-server startup passes. Client startup proceeds beyond Fabric mod loading and mixin application to graphics-backend creation, where this execution environment cannot provide OpenGL 3.3 or a usable Vulkan physical device; therefore main-menu rendering could not be evidenced in this environment.

### Test-first correction

Added `ModConfigTest` before production code and ran:

```bash
./gradlew test --tests com.smushytaco.expanded_axe_enchanting.ModConfigTest --no-daemon
```

The red run failed at `compileTestJava` with four expected `cannot find symbol: method createAndLoad(Path)` errors. The repository-owned implementation was then added and the same command passed (`BUILD SUCCESSFUL in 18s`). The focused tests cover all seven default-true values and their initial persisted JSON, loading all seven false values, and saving/reloading changed values. Existing `AxeEnchantmentPolicyTest` remains unchanged and passes.

### Implementation

- Replaced owo's generated `ModConfig` with a minimal Kotlin `ModConfig` that preserves the seven boolean properties, Java getter/setter API, default `true` values, `createAndLoad()` entry point, and JSON persistence at Fabric's config directory.
- Added a `Path` overload for isolated persistence tests. Missing individual JSON keys retain `true`, preserving defaults for older/partial files.
- Removed owo runtime, KSP plugin/processor, the wispforest repository, version-catalog aliases, generated-configuration annotation source, and Modrinth required owo declaration.
- Removed the now-dead `ChatFormatting` bridge, its mixin registration, and its sample-only tests.
- Removed owo from `fabric.mod.json` and bounded Minecraft compatibility to `~26.2` instead of accepting all future versions.

### Verification commands and results

```bash
./gradlew clean build --no-daemon
```

Exit 0, `BUILD SUCCESSFUL in 17s`; all tests passed and distributable artifacts were produced.

```bash
./gradlew runServer --no-daemon
```

Reached:

```text
Starting minecraft server version 26.2
Done (0.922s)! For help, type "help"
```

The process was explicitly stopped afterward.

```bash
./gradlew runClient --no-daemon
```

The client reached `Setting user: Player400` and LWJGL backend initialization with no owo mixin, project mixin, mod initialization, or configuration failure. It then failed to create a graphics backend because the environment reports `Driver does not support OpenGL 3.3` and Vulkan `VK_ERROR_OUT_OF_HOST_MEMORY`. A second software-Mesa attempt with `MESA_GL_VERSION_OVERRIDE=4.6 MESA_GLSL_VERSION_OVERRIDE=460 LIBGL_ALWAYS_SOFTWARE=true` reached the same environment failure. Both processes were explicitly stopped. This is distinct from the repaired owo failure and prevents honest main-menu evidence here.

```bash
./gradlew clean test build --no-daemon
```

Final fresh result: exit 0, `BUILD SUCCESSFUL in 22s`, 9 actionable tasks executed. Warnings were existing Gradle/Loom deprecations, JOML's terminally deprecated `Unsafe` call, and Trae sandbox `/proc/.../coredump_filter` restrictions after successful completion.

IDE diagnostics returned no findings. `git diff --check` returned no output. Process inspection found no remaining `KnotClient`, `KnotServer`, or Minecraft server process. The emitted root-level client log was deleted; standard ignored `run/` artifacts remain. `MIGRATION_26.2.md` was never touched or staged.

### Files

- `/mnt/Random/users/user/Documents/GitHub/Expanded-Axe-Enchanting/Expanded Axe Enchanting/src/main/kotlin/com/smushytaco/expanded_axe_enchanting/ModConfig.kt`
- `/mnt/Random/users/user/Documents/GitHub/Expanded-Axe-Enchanting/Expanded Axe Enchanting/src/test/java/com/smushytaco/expanded_axe_enchanting/ModConfigTest.java`
- `/mnt/Random/users/user/Documents/GitHub/Expanded-Axe-Enchanting/Expanded Axe Enchanting/build.gradle.kts`
- `/mnt/Random/users/user/Documents/GitHub/Expanded-Axe-Enchanting/Expanded Axe Enchanting/gradle/libs.versions.toml`
- `/mnt/Random/users/user/Documents/GitHub/Expanded-Axe-Enchanting/Expanded Axe Enchanting/src/main/resources/fabric.mod.json`
- `/mnt/Random/users/user/Documents/GitHub/Expanded-Axe-Enchanting/Expanded Axe Enchanting/src/main/resources/expanded_axe_enchanting.mixins.json`
- Deleted obsolete owo annotation/bridge sources and bridge test listed by the covering commit.

### Self-review and concerns

The change is deliberately limited to replacing the unsupported configuration dependency and deleting compatibility code made dead by that replacement. The policy API and behavior are preserved. No unrelated UI replacement was introduced; consequently the previous owo-provided Mod Menu screen is no longer present, while file persistence remains supported as required. The only unresolved verification concern is environmental graphics capability preventing main-menu evidence; no dependency or mod exception occurs before that point.

### Commit

Covering commit: `fix: remove incompatible owo runtime` (final hash reported with task result).
