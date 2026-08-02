package com.smushytaco.expanded_axe_enchanting

import net.fabricmc.api.ModInitializer

object ExpandedAxeEnchanting : ModInitializer {
    const val MOD_ID = "expanded_axe_enchanting"
    val config = ModConfig.createAndLoad()
    override fun onInitialize() = Unit
}