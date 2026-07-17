package com.overstacked.network;

import com.overstacked.Overstacked;
import com.overstacked.config.OverstackedConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientNetworkHandler {

    private ClientNetworkHandler() {}

    public static void handleConfigSync(ConfigSyncPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            OverstackedConfig.getInstance().applyFromPayload(payload);
            Overstacked.LOGGER.info("[Overstacked] Server sync applied — defaultMax={}",
                    payload.defaultMaxStackSize());
        });
    }
}
