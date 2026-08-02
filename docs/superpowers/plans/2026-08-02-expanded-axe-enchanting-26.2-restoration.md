# Expanded Axe Enchanting 26.2 Restoration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate mod to Minecraft 26.2 and restore seven configurable axe-enchantment applicability rules through current holder-aware APIs.

**Architecture:** Resolve exact 26.2 runtime API first, then inject at narrow applicability decision carrying enchantment identity. Delegate policy to pure Java helper so full behavior matrix is unit-testable without server lifecycle state; integration launch verifies transformed mixin target.

**Tech Stack:** Java 25, Kotlin 2.3.20, Gradle 9.5, Fabric Loom 1.17-SNAPSHOT, Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.152.1+26.2, owo-lib, Mixin Extras, JUnit 5.

## Global Constraints

- Preserve all seven existing config toggles and defaults.
- Return vanilla result for non-axes, unrelated enchantments, disabled options, and vanilla-positive cases.
- Use exact `ResourceKey<Enchantment>`/`Holder<Enchantment>` identity; no global item-tag mutation or complete enchantment JSON override.
- Remove server-cached `RegistryAccess` after replacement.
- Do not restore historical feature-specific mixins, Cloth configuration, or explicit MixinExtras bootstrap.
- Never stage or commit untracked `MIGRATION_26.2.md`.
- Keep Java 25 and non-remapping Loom with no mappings dependency.

---

### Task 1: Resolve Minecraft 26.2 API

**Files:**
- Modify: `Expanded Axe Enchanting/gradle/libs.versions.toml:5,24-32`
- Inspect: Gradle-resolved Minecraft 26.2 sources/classes and Loom mappings cache

**Interfaces:**
- Consumes: current non-remap Loom build.
- Produces: exact owner, method name, descriptor, argument positions, and return type for holder-aware item applicability hook.

- [ ] **Step 1: Update only verified 26.2 dependency versions**

Set:

```toml
loom = "1.17-SNAPSHOT"
minecraft = "26.2"
loader = "0.19.3"
fabric-api = "0.152.1+26.2"
```

Resolve current 26.2-compatible Fabric Language Kotlin and owo versions from configured repositories only when existing values fail dependency resolution; avoid unrelated upgrades.

- [ ] **Step 2: Resolve dependencies and compile to expose real API failures**

Run:

```bash
./gradlew compileJava compileKotlin --refresh-dependencies --no-daemon
```

Expected: dependencies resolve; existing `canEnchant` callback may compile because annotation target strings are runtime-resolved.

- [ ] **Step 3: Generate/read resolved 26.2 sources**

Run:

```bash
./gradlew genSources --no-daemon
```

Inspect `Enchantment`, `EnchantmentHelper`, enchanting-menu logic, and item applicability methods. Record exact candidate accepting both enchantment holder/key identity and `ItemStack`, preferring a boolean-returning method whose original result can be preserved.

- [ ] **Step 4: Prove selected target exists**

Use resolved sources/bytecode to verify exact descriptor and invocation sites. Reject targets that lack exact enchantment identity, execute only server-side, or globally mutate tags.

- [ ] **Step 5: Verify build metadata**

Run:

```bash
./gradlew dependencies --configuration runtimeClasspath --no-daemon
```

Expected: Minecraft 26.2, Loader 0.19.3, Fabric API 0.152.1+26.2; no mappings dependency failure.

- [ ] **Step 6: Commit toolchain migration**

```bash
git add "Expanded Axe Enchanting/gradle/libs.versions.toml"
git commit -m "build: migrate to Minecraft 26.2"
```

Do not stage `MIGRATION_26.2.md`.

---

### Task 2: Add Failing Policy Tests

**Files:**
- Modify: `Expanded Axe Enchanting/build.gradle.kts:78-85,113-132`
- Modify: `Expanded Axe Enchanting/gradle/libs.versions.toml`
- Create: `Expanded Axe Enchanting/src/main/java/com/smushytaco/expanded_axe_enchanting/AxeEnchantmentPolicy.java`
- Create: `Expanded Axe Enchanting/src/test/java/com/smushytaco/expanded_axe_enchanting/AxeEnchantmentPolicyTest.java`

**Interfaces:**
- Consumes: seven vanilla `ResourceKey<Enchantment>` constants and existing generated `ModConfig` getters.
- Produces: `AxeEnchantmentPolicy.extendApplicability(boolean original, ItemStack stack, ResourceKey<Enchantment> enchantmentKey, ModConfig config): boolean`.

- [ ] **Step 1: Add JUnit 5 test dependency and platform configuration**

Add version-catalog JUnit Jupiter dependency and:

```kotlin
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
```

Keep existing UTF-8 test configuration.

- [ ] **Step 2: Write failing parameterized tests for seven enabled enchantments**

Create cases for `FIRE_ASPECT`, `KNOCKBACK`, `LOOTING`, `IMPALING`, `DENSITY`, `BREACH`, and `WIND_BURST`. For each case, pass `original=false`, an axe stack, enabled config, and assert `true`.

- [ ] **Step 3: Write disabled-option tests**

For each key, disable only corresponding generated config option and assert `false` when `original=false` and stack is an axe. Reset configuration after each test.

- [ ] **Step 4: Write preservation tests**

Assert:

```java
assertTrue(extendApplicability(true, axe, unrelatedKey, config));
assertFalse(extendApplicability(false, nonAxe, supportedKey, config));
assertFalse(extendApplicability(false, axe, unrelatedKey, config));
```

Use one vanilla unrelated enchantment key and real `ItemStack` instances.

- [ ] **Step 5: Run tests and confirm expected failure**

Run:

```bash
./gradlew test --tests '*AxeEnchantmentPolicyTest' --no-daemon
```

Expected: FAIL because `AxeEnchantmentPolicy` or `extendApplicability` lacks implementation.

---

### Task 3: Implement Holder-Aware Applicability

**Files:**
- Modify: `Expanded Axe Enchanting/src/main/java/com/smushytaco/expanded_axe_enchanting/AxeEnchantmentPolicy.java`
- Modify: `Expanded Axe Enchanting/src/main/java/com/smushytaco/expanded_axe_enchanting/mixins/AxeModification.java`
- Modify: `Expanded Axe Enchanting/src/main/kotlin/com/smushytaco/expanded_axe_enchanting/ExpandedAxeEnchanting.kt`
- Modify: `Expanded Axe Enchanting/src/main/resources/expanded_axe_enchanting.mixins.json` only if selected owner requires renamed mixin class
- Test: `Expanded Axe Enchanting/src/test/java/com/smushytaco/expanded_axe_enchanting/AxeEnchantmentPolicyTest.java`

**Interfaces:**
- Consumes: selected Task 1 holder-aware target and `Holder<Enchantment>.is(ResourceKey<Enchantment>)` or equivalent exact-key API.
- Produces: transformed runtime applicability preserving original result and requiring no registry lifecycle cache.

- [ ] **Step 1: Implement minimal policy helper**

Implement this decision order:

```java
if (original || !(stack.getItem() instanceof AxeItem)) return original;
if (enchantmentKey == Enchantments.FIRE_ASPECT) return config.getCanUseFireAspectOnAxe();
if (enchantmentKey == Enchantments.KNOCKBACK) return config.getCanUseKnockbackOnAxe();
if (enchantmentKey == Enchantments.LOOTING) return config.getCanUseLootingOnAxe();
if (enchantmentKey == Enchantments.IMPALING) return config.getCanUseImpalingOnAxe();
if (enchantmentKey == Enchantments.DENSITY) return config.getCanUseDensityOnAxe();
if (enchantmentKey == Enchantments.BREACH) return config.getCanUseBreachOnAxe();
if (enchantmentKey == Enchantments.WIND_BURST) return config.getCanUseWindBurstOnAxe();
return false;
```

Use key matching API appropriate to `ResourceKey` semantics if reference identity is not guaranteed.

- [ ] **Step 2: Run policy tests**

Run:

```bash
./gradlew test --tests '*AxeEnchantmentPolicyTest' --no-daemon
```

Expected: PASS for complete behavior matrix.

- [ ] **Step 3: Replace obsolete mixin target**

Change `@ModifyReturnValue(method = "canEnchant", ...)` to Task 1 exact owner/method/descriptor. Capture `Holder<Enchantment>` or exact key and `ItemStack`; derive key through holder identity. Delegate only final decision to `AxeEnchantmentPolicy.extendApplicability`.

- [ ] **Step 4: Remove superseded dead code**

Reduce initializer to config ownership and mod ID:

```kotlin
object ExpandedAxeEnchanting : ModInitializer {
    const val MOD_ID = "expanded_axe_enchanting"
    val config = ModConfig.createAndLoad()
    override fun onInitialize() = Unit
}
```

If Fabric accepts initializer object without work, retain empty override because entrypoint contract invokes it. Remove `dynamicRegistryManager`, `SERVER_STARTED`, `RegistryAccess`, `Registries`, `ResourceKey`, `Enchantment`, optional helper imports, and `isSameEnchantment`.

- [ ] **Step 5: Compile exact mixin signature**

Run:

```bash
./gradlew compileJava compileKotlin --no-daemon
```

Expected: BUILD SUCCESSFUL with no stale 26.1.2 imports or signatures.

- [ ] **Step 6: Run all tests**

Run:

```bash
./gradlew test --no-daemon
```

Expected: all tests PASS.

- [ ] **Step 7: Commit restoration**

```bash
git add "Expanded Axe Enchanting/build.gradle.kts" \
  "Expanded Axe Enchanting/gradle/libs.versions.toml" \
  "Expanded Axe Enchanting/src/main/java/com/smushytaco/expanded_axe_enchanting/AxeEnchantmentPolicy.java" \
  "Expanded Axe Enchanting/src/main/java/com/smushytaco/expanded_axe_enchanting/mixins/AxeModification.java" \
  "Expanded Axe Enchanting/src/main/kotlin/com/smushytaco/expanded_axe_enchanting/ExpandedAxeEnchanting.kt" \
  "Expanded Axe Enchanting/src/test/java/com/smushytaco/expanded_axe_enchanting/AxeEnchantmentPolicyTest.java"
git commit -m "fix: restore axe enchantment applicability"
```

Add mixin JSON explicitly only if changed.

---

### Task 4: Runtime and Regression Verification

**Files:**
- Modify only files implicated by distinct verification failures.

**Interfaces:**
- Consumes: built 26.2 mod and required mixin configuration.
- Produces: evidence that mixin applies and startup succeeds on logical server and client.

- [ ] **Step 1: Build distributable artifact**

Run:

```bash
./gradlew clean build --no-daemon
```

Expected: BUILD SUCCESSFUL; jar and sources jar produced.

- [ ] **Step 2: Start Fabric server smoke test**

Run server non-interactively with accepted EULA in disposable run directory using existing Loom server run. Wait until startup completes or first decisive error. Expected: no `InvalidInjectionException`, missing target, registry lifecycle exception, or mod initialization failure.

- [ ] **Step 3: Start client smoke test**

Use ordinary `runClient` when offline launch works. Use `runClientAuth` only when existing Prism credentials were auto-discovered; never print tokens. Expected: main menu loads with no mixin application failure.

- [ ] **Step 4: Inspect diagnostics and final diff**

Run IDE diagnostics. Then:

```bash
git status --short
git diff HEAD~2..HEAD --check
git log --oneline -5
```

Expected: no whitespace errors; only intended commits/files; `MIGRATION_26.2.md` remains untracked.

- [ ] **Step 5: Fix distinct verification defects test-first**

For each failure, add smallest reproducing test where feasible, verify failure, apply minimal correction, rerun affected test plus full build. Commit separately only when correction is logically distinct:

```bash
git commit -m "fix: correct 26.2 runtime integration"
```

- [ ] **Step 6: Final verification**

Run:

```bash
./gradlew test build --no-daemon
git status --short --branch
```

Expected: tests/build pass; branch contains focused commits; only unrelated `MIGRATION_26.2.md` remains untracked.
