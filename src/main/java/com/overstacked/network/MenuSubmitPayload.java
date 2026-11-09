package com.overstacked.network;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MenuSubmitPayload(
        int defaultMaxStackSize,
        boolean stackEnchantedBooks,
        boolean stackEnderPearlsSnowballs,
        boolean stackBucketsPotions,
        boolean stackTotems,
        boolean stackToolsArmor,
        Map<String, Integer> itemOverrides,
        Map<String, Integer> tagOverrides
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MenuSubmitPayload> ID =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("overstacked", "menu_submit"));

    public static final StreamCodec<FriendlyByteBuf, MenuSubmitPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public MenuSubmitPayload decode(FriendlyByteBuf buf) {
                    int defaultMax = buf.readInt();
                    boolean eb  = buf.readBoolean();
                    boolean eps = buf.readBoolean();
                    boolean bp  = buf.readBoolean();
                    boolean tot = buf.readBoolean();
                    boolean ta  = buf.readBoolean();

                    int itemCount = buf.readVarInt();
                    Map<String, Integer> items = new LinkedHashMap<>(itemCount);
                    for (int i = 0; i < itemCount; i++) {
                        items.put(buf.readUtf(), buf.readVarInt());
                    }

                    int tagCount = buf.readVarInt();
                    Map<String, Integer> tags = new LinkedHashMap<>(tagCount);
                    for (int i = 0; i < tagCount; i++) {
                        tags.put(buf.readUtf(), buf.readVarInt());
                    }

                    return new MenuSubmitPayload(defaultMax, eb, eps, bp, tot, ta, items, tags);
                }

                @Override
                public void encode(FriendlyByteBuf buf, MenuSubmitPayload payload) {
                    buf.writeInt(payload.defaultMaxStackSize);
                    buf.writeBoolean(payload.stackEnchantedBooks);
                    buf.writeBoolean(payload.stackEnderPearlsSnowballs);
                    buf.writeBoolean(payload.stackBucketsPotions);
                    buf.writeBoolean(payload.stackTotems);
                    buf.writeBoolean(payload.stackToolsArmor);

                    buf.writeVarInt(payload.itemOverrides.size());
                    payload.itemOverrides.forEach((key, value) -> {
                        buf.writeUtf(key);
                        buf.writeVarInt(value);
                    });

                    buf.writeVarInt(payload.tagOverrides.size());
                    payload.tagOverrides.forEach((key, value) -> {
                        buf.writeUtf(key);
                        buf.writeVarInt(value);
                    });
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
