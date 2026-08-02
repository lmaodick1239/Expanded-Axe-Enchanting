package com.smushytaco.expanded_axe_enchanting.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.smushytaco.expanded_axe_enchanting.AxeEnchantmentPolicy;
import com.smushytaco.expanded_axe_enchanting.ExpandedAxeEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public abstract class AxeModification {
    @ModifyReturnValue(
            method = "lambda$getAvailableEnchantmentResults$0(Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/core/Holder;)Z",
            at = @At("RETURN")
    )
    private static boolean extendAxeApplicability(
            boolean original,
            ItemStack stack,
            boolean isBook,
            Holder<Enchantment> enchantment
    ) {
        ResourceKey<Enchantment> enchantmentKey = null;
        if (enchantment.is(Enchantments.FIRE_ASPECT)) enchantmentKey = Enchantments.FIRE_ASPECT;
        else if (enchantment.is(Enchantments.KNOCKBACK)) enchantmentKey = Enchantments.KNOCKBACK;
        else if (enchantment.is(Enchantments.LOOTING)) enchantmentKey = Enchantments.LOOTING;
        else if (enchantment.is(Enchantments.IMPALING)) enchantmentKey = Enchantments.IMPALING;
        else if (enchantment.is(Enchantments.DENSITY)) enchantmentKey = Enchantments.DENSITY;
        else if (enchantment.is(Enchantments.BREACH)) enchantmentKey = Enchantments.BREACH;
        else if (enchantment.is(Enchantments.WIND_BURST)) enchantmentKey = Enchantments.WIND_BURST;

        return AxeEnchantmentPolicy.extendApplicability(
                original,
                stack,
                enchantmentKey,
                ExpandedAxeEnchanting.INSTANCE.getConfig()
        );
    }
}