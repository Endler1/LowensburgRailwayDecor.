package de.lowensburg.decor;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class LowensburgRailwayDecor implements ModInitializer {
    public static final String MOD_ID = "lowensburg_railway_decor";

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModItemGroup.register();
    }
}
