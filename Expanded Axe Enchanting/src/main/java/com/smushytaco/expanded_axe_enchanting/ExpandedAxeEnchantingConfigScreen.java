package com.smushytaco.expanded_axe_enchanting;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

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
        addToggle(x, y, "canUseFireAspectOnAxe", config::getCanUseFireAspectOnAxe, config::setCanUseFireAspectOnAxe);
        addToggle(x + 160, y, "canUseKnockbackOnAxe", config::getCanUseKnockbackOnAxe, config::setCanUseKnockbackOnAxe);
        addToggle(x, y + 24, "canUseLootingOnAxe", config::getCanUseLootingOnAxe, config::setCanUseLootingOnAxe);
        addToggle(x + 160, y + 24, "canUseImpalingOnAxe", config::getCanUseImpalingOnAxe, config::setCanUseImpalingOnAxe);
        addToggle(x, y + 48, "canUseDensityOnAxe", config::getCanUseDensityOnAxe, config::setCanUseDensityOnAxe);
        addToggle(x + 160, y + 48, "canUseBreachOnAxe", config::getCanUseBreachOnAxe, config::setCanUseBreachOnAxe);
        addToggle(x, y + 72, "canUseWindBurstOnAxe", config::getCanUseWindBurstOnAxe, config::setCanUseWindBurstOnAxe);
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 - 100, y + 112, 200, 20)
                .build());
    }

    private void addToggle(int x, int y, String key, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        addRenderableWidget(CycleButton.onOffBuilder(getter.get())
                .create(x, y, 150, 20, Component.translatable(KEY_PREFIX + key), (button, value) -> setter.accept(value)));
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
