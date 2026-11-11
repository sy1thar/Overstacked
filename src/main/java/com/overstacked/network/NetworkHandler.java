package com.overstacked.network;

import com.overstacked.Overstacked;
import com.overstacked.config.OverstackedConfig;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class NetworkHandler {

    private NetworkHandler() {}

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MenuSubmitPayload.ID, MenuSubmitPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(MenuSubmitPayload.ID, ServerNetworkHandler::handleMenuSubmit);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            OverstackedConfig config = OverstackedConfig.getInstance();
            if (config.isLoaded()) {
                ServerPlayNetworking.send(handler.getPlayer(), config.toPayload());
                Overstacked.LOGGER.info("[Overstacked] Synced config to joining player: {}",
                        handler.getPlayer().getName().getString());
            }
        });

        Overstacked.LOGGER.info("[Overstacked] Networking registered");
    }
}
