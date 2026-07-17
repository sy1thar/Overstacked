package com.overstacked;

import com.overstacked.config.OverstackedConfig;
import com.overstacked.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Overstacked implements ModInitializer {

    public static final String MOD_ID = "overstacked";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Overstacked] Initializing …");

        OverstackedConfig.getInstance().load();
        NetworkHandler.register();

        LOGGER.info("[Overstacked] Ready — defaultMaxStackSize={}",
                OverstackedConfig.getInstance().getDefaultMaxStackSize());
    }
}
