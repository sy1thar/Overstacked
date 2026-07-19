package com.overstacked.network;

import com.overstacked.Overstacked;
import com.overstacked.config.OverstackedConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ServerNetworkHandler {

    private ServerNetworkHandler() {}

    public static void handleMenuSubmit(MenuSubmitPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        context.player().server.execute(() -> {
            OverstackedConfig config = OverstackedConfig.getInstance();
            config.setDefaultMaxStackSize(payload.defaultMaxStackSize());
            config.setStackEnchantedBooks(payload.stackEnchantedBooks());
            config.setStackEnderPearlsSnowballs(payload.stackEnderPearlsSnowballs());
            config.setStackBucketsPotions(payload.stackBucketsPotions());
            config.setStackTotems(payload.stackTotems());
            config.setStackToolsArmor(payload.stackToolsArmor());
            config.clearItemOverrides();
            config.clearTagOverrides();
            config.reloadDiskRules();

            payload.itemOverrides().forEach(config::addItemOverride);
            payload.tagOverrides().forEach(config::addTagOverride);

            config.saveToDisk();

            Overstacked.LOGGER.info("[Overstacked] Menu submit from {}: defaultMax={}, itemOverrides={}, tagOverrides={}",
                    player.getName().getString(), payload.defaultMaxStackSize(),
                    payload.itemOverrides().size(), payload.tagOverrides().size());

            ConfigSyncPayload syncPayload = config.toPayload();
            context.server().getPlayerManager().getPlayerList().forEach(p ->
                    ServerPlayNetworking.send(p, syncPayload));
        });
    }
}
