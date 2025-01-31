package net.backupcup.mcd_enchantments;

import net.backupcup.mcd_enchantments.block.ModBlocks;
import net.backupcup.mcd_enchantments.screen.GildingFoundryScreen;
import net.backupcup.mcd_enchantments.screen.ModScreenHandlers;
import net.backupcup.mcd_enchantments.screen.RerollStationScreen;
import net.backupcup.mcd_enchantments.screen.RunicTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;

public class MCDEClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.RUNIC_TABLE_SCREEN_HANDLER, RunicTableScreen::new);
        HandledScreens.register(ModScreenHandlers.REROLL_STATION_SCREEN_HANDLER, RerollStationScreen::new);
        HandledScreens.register(ModScreenHandlers.GILDING_FOUNDRY_SCREEN_HANDLER, GildingFoundryScreen::new);

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.RUNIC_TABLE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.REROLL_STATION, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GILDING_FOUNDRY, RenderLayer.getCutout());
    }
}
