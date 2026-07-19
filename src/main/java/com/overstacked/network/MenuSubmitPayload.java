package com.overstacked.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public record MenuSubmitPayload(
        int defaultMaxStackSize,
        boolean stackEnchantedBooks,
        boolean stackEnderPearlsSnowballs,
        boolean stackBucketsPotions,
        boolean stackTotems,
        boolean stackToolsArmor,
        Map<String, Integer> itemOverrides,
        Map<String, Integer> tagOverrides
) implements CustomPayload {

    public static final CustomPayload.Id<MenuSubmitPayload> ID =
            new CustomPayload.Id<>(Identifier.of("overstacked", "menu_submit"));

    public static final PacketCodec<PacketByteBuf, MenuSubmitPayload> CODEC =
            new PacketCodec<>() {
                @Override
                public MenuSubmitPayload decode(PacketByteBuf buf) {
                    int defaultMax = buf.readInt();
                    boolean eb  = buf.readBoolean();
                    boolean eps = buf.readBoolean();
                    boolean bp  = buf.readBoolean();
                    boolean tot = buf.readBoolean();
                    boolean ta  = buf.readBoolean();

                    int itemCount = buf.readVarInt();
                    Map<String, Integer> items = new LinkedHashMap<>(itemCount);
                    for (int i = 0; i < itemCount; i++) {
                        items.put(buf.readString(), buf.readVarInt());
                    }

                    int tagCount = buf.readVarInt();
                    Map<String, Integer> tags = new LinkedHashMap<>(tagCount);
                    for (int i = 0; i < tagCount; i++) {
                        tags.put(buf.readString(), buf.readVarInt());
                    }

                    return new MenuSubmitPayload(defaultMax, eb, eps, bp, tot, ta, items, tags);
                }

                @Override
                public void encode(PacketByteBuf buf, MenuSubmitPayload payload) {
                    buf.writeInt(payload.defaultMaxStackSize);
                    buf.writeBoolean(payload.stackEnchantedBooks);
                    buf.writeBoolean(payload.stackEnderPearlsSnowballs);
                    buf.writeBoolean(payload.stackBucketsPotions);
                    buf.writeBoolean(payload.stackTotems);
                    buf.writeBoolean(payload.stackToolsArmor);

                    buf.writeVarInt(payload.itemOverrides.size());
                    payload.itemOverrides.forEach((key, value) -> {
                        buf.writeString(key);
                        buf.writeVarInt(value);
                    });

                    buf.writeVarInt(payload.tagOverrides.size());
                    payload.tagOverrides.forEach((key, value) -> {
                        buf.writeString(key);
                        buf.writeVarInt(value);
                    });
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
