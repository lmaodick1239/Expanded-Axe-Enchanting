package com.smushytaco.expanded_axe_enchanting;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class AxeEnchantmentPolicy {
    private AxeEnchantmentPolicy() {
    }

    public static boolean extendApplicability(
            boolean original,
            ItemStack stack,
            ResourceKey<Enchantment> enchantmentKey,
            ModConfig config
    ) {
        if (original || !(stack.getItem() instanceof AxeItem)) return original;
        if (Enchantments.FIRE_ASPECT.equals(enchantmentKey)) return config.getCanUseFireAspectOnAxe();
        if (Enchantments.KNOCKBACK.equals(enchantmentKey)) return config.getCanUseKnockbackOnAxe();
        if (Enchantments.LOOTING.equals(enchantmentKey)) return config.getCanUseLootingOnAxe();
        if (Enchantments.IMPALING.equals(enchantmentKey)) return config.getCanUseImpalingOnAxe();
        if (Enchantments.DENSITY.equals(enchantmentKey)) return config.getCanUseDensityOnAxe();
        if (Enchantments.BREACH.equals(enchantmentKey)) return config.getCanUseBreachOnAxe();
        if (Enchantments.WIND_BURST.equals(enchantmentKey)) return config.getCanUseWindBurstOnAxe();
        return false;
    }
}
