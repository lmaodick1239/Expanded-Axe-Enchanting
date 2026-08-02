package com.smushytaco.expanded_axe_enchanting;

import net.minecraft.ChatFormatting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatFormattingCompatibilityTest {
    @Test
    void exposesLegacyColorMetadataRequiredByOwo() {
        assertTrue(ChatFormattingCompatibility.isColor(ChatFormatting.DARK_RED));
        assertEquals(0xAA0000, ChatFormattingCompatibility.color(ChatFormatting.DARK_RED));
        assertEquals("dark_red", ChatFormattingCompatibility.name(ChatFormatting.DARK_RED));

        assertFalse(ChatFormattingCompatibility.isColor(ChatFormatting.BOLD));
        assertNull(ChatFormattingCompatibility.color(ChatFormatting.BOLD));
        assertFalse(ChatFormattingCompatibility.isColor(ChatFormatting.RESET));
    }
}
