package com.smushytaco.expanded_axe_enchanting.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.smushytaco.expanded_axe_enchanting.AxeEnchantmentPolicy;
import com.smushytaco.expanded_axe_enchanting.ExpandedAxeEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
        return AxeEnchantmentPolicy.extendApplicability(
                original,
                stack,
                enchantment,
                ExpandedAxeEnchanting.INSTANCE.getConfig()
        );
    }
}