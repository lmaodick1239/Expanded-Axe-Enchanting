package com.smushytaco.expanded_axe_enchanting;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class ExpandedAxeEnchantingModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ExpandedAxeEnchantingConfigScreen::new;
    }
}
