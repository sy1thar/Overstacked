package com.overstacked.network;

import com.overstacked.Overstacked;
import com.overstacked.config.OverstackedConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class NetworkHandler {

    private NetworkHandler() {}

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MenuSubmitPayload.ID, MenuSubmitPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.ID, ClientNetworkHandler::handleConfigSync);
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
