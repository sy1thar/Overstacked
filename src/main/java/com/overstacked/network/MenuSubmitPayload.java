package com.overstacked.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MenuSubmitPayload(
        int defaultMaxStackSize,
        boolean stackEnchantedBooks,
        boolean stackEnderPearlsSnowballs,
        boolean stackBucketsPotions,
        boolean stackTotems,
        boolean stackToolsArmor
) implements CustomPayload {

    public static final CustomPayload.Id<MenuSubmitPayload> ID =
            new CustomPayload.Id<>(Identifier.of("overstacked", "menu_submit"));

    public static final PacketCodec<PacketByteBuf, MenuSubmitPayload> CODEC =
            new PacketCodec<>() {
                @Override
                public MenuSubmitPayload decode(PacketByteBuf buf) {
                    return new MenuSubmitPayload(
                            buf.readInt(),
                            buf.readBoolean(), buf.readBoolean(),
                            buf.readBoolean(), buf.readBoolean(),
                            buf.readBoolean());
                }

                @Override
                public void encode(PacketByteBuf buf, MenuSubmitPayload payload) {
                    buf.writeInt(payload.defaultMaxStackSize);
                    buf.writeBoolean(payload.stackEnchantedBooks);
                    buf.writeBoolean(payload.stackEnderPearlsSnowballs);
                    buf.writeBoolean(payload.stackBucketsPotions);
                    buf.writeBoolean(payload.stackTotems);
                    buf.writeBoolean(payload.stackToolsArmor);
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
