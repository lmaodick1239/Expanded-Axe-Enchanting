package com.smushytaco.expanded_axe_enchanting;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public record ConfigToggle(String key, Predicate<ModConfig> getter, BiConsumer<ModConfig, Boolean> setter) {
    private static final List<ConfigToggle> ALL = List.of(
            new ConfigToggle("canUseFireAspectOnAxe", ModConfig::getCanUseFireAspectOnAxe, ModConfig::setCanUseFireAspectOnAxe),
            new ConfigToggle("canUseKnockbackOnAxe", ModConfig::getCanUseKnockbackOnAxe, ModConfig::setCanUseKnockbackOnAxe),
            new ConfigToggle("canUseLootingOnAxe", ModConfig::getCanUseLootingOnAxe, ModConfig::setCanUseLootingOnAxe),
            new ConfigToggle("canUseImpalingOnAxe", ModConfig::getCanUseImpalingOnAxe, ModConfig::setCanUseImpalingOnAxe),
            new ConfigToggle("canUseDensityOnAxe", ModConfig::getCanUseDensityOnAxe, ModConfig::setCanUseDensityOnAxe),
            new ConfigToggle("canUseBreachOnAxe", ModConfig::getCanUseBreachOnAxe, ModConfig::setCanUseBreachOnAxe),
            new ConfigToggle("canUseWindBurstOnAxe", ModConfig::getCanUseWindBurstOnAxe, ModConfig::setCanUseWindBurstOnAxe)
    );

    public boolean get(ModConfig config) {
        return getter.test(config);
    }

    public void set(ModConfig config, boolean value) {
        setter.accept(config, value);
    }

    public static List<ConfigToggle> all() {
        return ALL;
    }
}
