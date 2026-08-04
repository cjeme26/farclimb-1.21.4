package com.cjeme26.farclimb;

import com.cjeme26.farclimb.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FarClimb implements ModInitializer {
    public static final String MOD_ID = "farclimb";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.initialize();
        LOGGER.info("FarClimb initialized.");
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
