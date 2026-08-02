package com.smushytaco.expanded_axe_enchanting;

import net.minecraft.ChatFormatting;

public final class ChatFormattingCompatibility {
    private ChatFormattingCompatibility() {
    }

    public static boolean isColor(ChatFormatting formatting) {
        return color(formatting) != null;
    }

    public static Integer color(ChatFormatting formatting) {
        return switch (formatting) {
            case BLACK -> 0x000000;
            case DARK_BLUE -> 0x0000AA;
            case DARK_GREEN -> 0x00AA00;
            case DARK_AQUA -> 0x00AAAA;
            case DARK_RED -> 0xAA0000;
            case DARK_PURPLE -> 0xAA00AA;
            case GOLD -> 0xFFAA00;
            case GRAY -> 0xAAAAAA;
            case DARK_GRAY -> 0x555555;
            case BLUE -> 0x5555FF;
            case GREEN -> 0x55FF55;
            case AQUA -> 0x55FFFF;
            case RED -> 0xFF5555;
            case LIGHT_PURPLE -> 0xFF55FF;
            case YELLOW -> 0xFFFF55;
            case WHITE -> 0xFFFFFF;
            default -> null;
        };
    }

    public static String name(ChatFormatting formatting) {
        return formatting.name().toLowerCase(java.util.Locale.ROOT);
    }
}
