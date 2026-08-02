package com.smushytaco.expanded_axe_enchanting;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ExpandedAxeEnchantingConfigScreen extends Screen {
    private static final String KEY_PREFIX = "text.config.expanded_axe_enchanting.option.";
    private final Screen parent;

    public ExpandedAxeEnchantingConfigScreen(Screen parent) {
        super(Component.translatable("text.config.expanded_axe_enchanting.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig config = ExpandedAxeEnchanting.INSTANCE.getConfig();
        int x = width / 2 - 155;
        int y = 40;
        for (int index = 0; index < ConfigToggle.all().size(); index++) {
            ConfigToggle toggle = ConfigToggle.all().get(index);
            addToggle(x + index % 2 * 160, y + index / 2 * 24, toggle, config);
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 - 100, y + 112, 200, 20)
                .build());
    }

    private void addToggle(int x, int y, ConfigToggle toggle, ModConfig config) {
        addRenderableWidget(CycleButton.onOffBuilder(toggle.get(config))
                .create(x, y, 150, 20, Component.translatable(KEY_PREFIX + toggle.key()),
                        (button, value) -> toggle.set(config, value)));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        ExpandedAxeEnchanting.INSTANCE.getConfig().save();
        minecraft.gui.setScreen(parent);
    }
}
