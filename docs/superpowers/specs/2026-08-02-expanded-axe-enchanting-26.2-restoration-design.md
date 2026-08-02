# Expanded Axe Enchanting 26.2 Restoration Design

## Goal

Migrate Expanded Axe Enchanting from Minecraft 26.1.2 to 26.2 and restore its seven advertised axe-enchantment extensions after the legacy `Enchantment.canEnchant` hook disappeared. Remove only code proven dead after replacement, preserve configuration behavior, and verify client/server compatibility without regressions.

## Audit Findings

Full reachable Git history, reflogs, refs, and unreachable-object checks contain no prior-agent commit or recoverable scrubbed implementation. Historical source deletions were valid replacements:

- Three feature-specific mixins became `AxeModification` in `f711f59`.
- Explicit MixinExtras prelaunch bootstrap became unnecessary in `6c79505`.
- Cloth/ModMenu configuration classes became owo-generated configuration in `1099044`.
- Yarn hooks and registry calls became official-mapping equivalents in `e45d52c`.

No current method is dead under the 26.1.2 implementation. Configuration fields are KSP inputs, not unused runtime code.

The actual missing implementation is structural: current required mixin targets `Enchantment.canEnchant`, absent from modern 26.x enchantment applicability. Registry-key matching also depends on server-start-cached `RegistryAccess`, causing pre-start and remote-client behavior to fall back to vanilla.

`MIGRATION_26.2.md` is untracked and describes Minecraft Server Properties Reload, not this repository. Only generally applicable verified facts may guide work: Java 25, Minecraft 26.2, non-remapping Loom, no mappings dependency, and matching Fabric versions. The file must not be committed.

## Selected Architecture

Use a narrow holder-aware runtime hook in the resolved Minecraft 26.2 applicability path.

1. Upgrade build inputs to exact 26.2-compatible versions.
2. Resolve and inspect 26.2 classes and method signatures.
3. Choose the smallest stable method that decides whether a specific enchantment holder supports an item stack.
4. Preserve the original result.
5. Return `true` only when:
   - the stack contains an `AxeItem`,
   - the enchantment holder matches one of the seven exact vanilla enchantment keys, and
   - that enchantment's existing config toggle is enabled.
6. Return the original result for disabled options, non-axes, unrelated enchantments, and all vanilla-positive cases.

Holder/key-aware matching avoids global mutable registry state and works identically on logical client and server.

## Rejected Alternatives

### Datapack enchantment overrides

Simple runtime behavior but replaces complete vanilla definitions, conflicts with other mods/datapacks, and cannot cleanly honor runtime config toggles.

### Broad item-tag mutation

Small implementation but grants axes every enchantment sharing the mutated tag. This exceeds the seven advertised features.

## Dead-Code Cleanup

After the new hook is identified and covered by tests, remove:

- obsolete `Enchantment.canEnchant` callback,
- `dynamicRegistryManager`,
- `SERVER_STARTED` registry capture,
- `isSameEnchantment`,
- imports used only by those elements,
- obsolete mixin metadata only if the replacement uses a different mixin class.

Do not remove owo configuration fields or generated-access patterns. Do not restore old feature-specific mixins, Cloth configuration integration, or MixinExtras bootstrap.

## Testing

Automated coverage must verify:

- Fire Aspect, Knockback, Looting, Impaling, Density, Breach, and Wind Burst accept axes when enabled.
- Each rejects axes through this extension when disabled.
- Non-axe items receive unchanged vanilla results.
- Unrelated enchantments receive unchanged vanilla results.
- Existing vanilla-positive applicability remains positive.
- Logic requires no server lifecycle initialization.
- Mixin target resolves under Minecraft 26.2.

Prefer a pure decision helper for the behavior matrix plus Fabric GameTest or equivalent integration coverage for actual 26.2 registry holders and transformed runtime. If framework constraints prevent one layer, preserve matrix tests and validate the transformed hook through launch smoke tests.

Verification sequence:

1. Compile Java and Kotlin.
2. Run automated tests.
3. Build distributable jar.
4. Launch server smoke test and inspect startup/mixin failures.
5. Launch client smoke test when available without credentials; otherwise use configured authenticated run only if local credentials already exist.
6. Review diagnostics and final diff.

## Commit Structure

Keep commits independently understandable:

1. `docs: record 26.2 restoration design`
2. `build: migrate to Minecraft 26.2` — toolchain/version changes and any cleanup inseparable from compilation.
3. `fix: restore axe enchantment applicability` — new hook, removal of superseded dead code, tests.
4. Additional focused fix commit only if verification exposes a distinct defect.

Never stage `MIGRATION_26.2.md` or unrelated user files.

## Success Criteria

- Project targets Minecraft 26.2 and Java 25 using correct non-remap Loom setup.
- No required mixin references a missing target.
- All seven configured features work for axes on client and server.
- Disabled toggles and unrelated vanilla behavior remain unchanged.
- Server lifecycle state is no longer required for enchantment identity.
- Automated tests and full build pass; runtime smoke tests show no bootstrap or mixin errors.
- Git history contains focused commits and working tree retains unrelated untracked files untouched.
