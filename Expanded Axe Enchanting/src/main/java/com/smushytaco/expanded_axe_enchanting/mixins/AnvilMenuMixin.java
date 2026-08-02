package com.smushytaco.expanded_axe_enchanting.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.smushytaco.expanded_axe_enchanting.AxeEnchantmentPolicy;
import com.smushytaco.expanded_axe_enchanting.ExpandedAxeEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    @WrapOperation(
            method = "createResult()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean extendAxeApplicability(
            Enchantment enchantment,
            ItemStack stack,
            Operation<Boolean> original,
            @Local Holder<Enchantment> enchantmentHolder
    ) {
        return AxeEnchantmentPolicy.extendApplicability(
                original.call(enchantment, stack),
                stack,
                enchantmentHolder,
                ExpandedAxeEnchanting.INSTANCE.getConfig()
        );
    }
}
