package com.overstacked.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ItemStack.class)
public class ItemStackPacketCodecMixin {

    @Overwrite
    public static PacketCodec<RegistryByteBuf, ItemStack> createExtraValidatingPacketCodec(PacketCodec<RegistryByteBuf, ItemStack> inner) {
        return inner;
    }
}
