package com.smushytaco.expanded_axe_enchanting.mixins;

import com.smushytaco.expanded_axe_enchanting.ChatFormattingCompatibility;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatFormatting.class)
public abstract class ChatFormattingCompatibilityMixin {
    public boolean isColor() {
        return ChatFormattingCompatibility.isColor((ChatFormatting) (Object) this);
    }

    public Integer getColor() {
        return ChatFormattingCompatibility.color((ChatFormatting) (Object) this);
    }

    public String getName() {
        return ChatFormattingCompatibility.name((ChatFormatting) (Object) this);
    }
}
