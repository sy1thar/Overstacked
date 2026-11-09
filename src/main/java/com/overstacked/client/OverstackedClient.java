package com.overstacked.client;

import com.overstacked.network.ClientNetworkHandler;
import com.overstacked.network.ConfigSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class OverstackedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.ID, ClientNetworkHandler::handleConfigSync);
    }
}
