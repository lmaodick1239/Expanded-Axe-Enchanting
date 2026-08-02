package com.smushytaco.expanded_axe_enchanting;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class AxeEnchantmentPolicyTest {
    private ModConfig config;
    private ItemStack axe;

    @BeforeEach
    void setUp() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        config = mock(ModConfig.class);
        axe = stackWith(mock(AxeItem.class));
        enableAllOptions();
    }

    @AfterEach
    void resetConfiguration() {
        reset(config);
    }

    @ParameterizedTest
    @MethodSource("supportedEnchantments")
    void enabledEnchantmentExtendsApplicabilityToAxes(ResourceKey<Enchantment> enchantmentKey) {
        assertTrue(AxeEnchantmentPolicy.extendApplicability(false, axe, enchantmentKey, config));
    }

    @ParameterizedTest
    @MethodSource("supportedEnchantmentsWithDisablers")
    void disabledOptionDoesNotExtendApplicabilityToAxes(
            ResourceKey<Enchantment> enchantmentKey,
            Consumer<ModConfig> disableOption
    ) {
        disableOption.accept(config);

        assertFalse(AxeEnchantmentPolicy.extendApplicability(false, axe, enchantmentKey, config));
    }

    @Test
    void preservesOriginalApplicability() {
        assertTrue(AxeEnchantmentPolicy.extendApplicability(true, axe, Enchantments.SHARPNESS, config));
    }

    @Test
    void doesNotExtendApplicabilityToNonAxes() {
        assertFalse(AxeEnchantmentPolicy.extendApplicability(
                false,
                stackWith(mock(Item.class)),
                Enchantments.FIRE_ASPECT,
                config
        ));
    }

    @Test
    void doesNotExtendApplicabilityForUnrelatedEnchantments() {
        assertFalse(AxeEnchantmentPolicy.extendApplicability(false, axe, Enchantments.SHARPNESS, config));
    }

    private static ItemStack stackWith(Item item) {
        Holder.Reference<Item> holder = Holder.Reference.createIntrusive(new HolderOwner<>() {}, item);
        holder.bindComponents(DataComponentMap.EMPTY);
        return new ItemStack(holder);
    }

    private void enableAllOptions() {
        when(config.getCanUseFireAspectOnAxe()).thenReturn(true);
        when(config.getCanUseKnockbackOnAxe()).thenReturn(true);
        when(config.getCanUseLootingOnAxe()).thenReturn(true);
        when(config.getCanUseImpalingOnAxe()).thenReturn(true);
        when(config.getCanUseDensityOnAxe()).thenReturn(true);
        when(config.getCanUseBreachOnAxe()).thenReturn(true);
        when(config.getCanUseWindBurstOnAxe()).thenReturn(true);
    }

    private static Stream<ResourceKey<Enchantment>> supportedEnchantments() {
        return Stream.of(
                Enchantments.FIRE_ASPECT,
                Enchantments.KNOCKBACK,
                Enchantments.LOOTING,
                Enchantments.IMPALING,
                Enchantments.DENSITY,
                Enchantments.BREACH,
                Enchantments.WIND_BURST
        );
    }

    private static Stream<Object[]> supportedEnchantmentsWithDisablers() {
        return Stream.of(
                new Object[]{Enchantments.FIRE_ASPECT, (Consumer<ModConfig>) config -> when(config.getCanUseFireAspectOnAxe()).thenReturn(false)},
                new Object[]{Enchantments.KNOCKBACK, (Consumer<ModConfig>) config -> when(config.getCanUseKnockbackOnAxe()).thenReturn(false)},
                new Object[]{Enchantments.LOOTING, (Consumer<ModConfig>) config -> when(config.getCanUseLootingOnAxe()).thenReturn(false)},
                new Object[]{Enchantments.IMPALING, (Consumer<ModConfig>) config -> when(config.getCanUseImpalingOnAxe()).thenReturn(false)},
                new Object[]{Enchantments.DENSITY, (Consumer<ModConfig>) config -> when(config.getCanUseDensityOnAxe()).thenReturn(false)},
                new Object[]{Enchantments.BREACH, (Consumer<ModConfig>) config -> when(config.getCanUseBreachOnAxe()).thenReturn(false)},
                new Object[]{Enchantments.WIND_BURST, (Consumer<ModConfig>) config -> when(config.getCanUseWindBurstOnAxe()).thenReturn(false)}
        );
    }
}
