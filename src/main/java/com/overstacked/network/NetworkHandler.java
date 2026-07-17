package com.overstacked.network;

import com.overstacked.Overstacked;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class NetworkHandler {

    private NetworkHandler() {}

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MenuSubmitPayload.ID, MenuSubmitPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.ID, ClientNetworkHandler::handleConfigSync);
        ServerPlayNetworking.registerGlobalReceiver(MenuSubmitPayload.ID, ServerNetworkHandler::handleMenuSubmit);

        Overstacked.LOGGER.info("[Overstacked] Networking registered");
    }
}
