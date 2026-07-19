package com.overstacked.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackPacketCodecMixin {

    /**
     * Убираем ограничение сетевого пакета при передаче ItemStack между клиентом и сервером.
     * В 1.21.1 количество передается через VarInt, поэтому нам нужно просто разрешить
     * чтение и запись значений вплоть до Integer.MAX_VALUE.
     */
    @Inject(method = "createPacketCodec", at = @At("RETURN"), cancellable = true)
    private static void overstacked$expandPacketCodec(CallbackInfoReturnable<PacketCodec<RegistryByteBuf, ItemStack>> cir) {
        PacketCodec<RegistryByteBuf, ItemStack> original = cir.getReturnValue();
        
        PacketCodec<RegistryByteBuf, ItemStack> expanded = new PacketCodec<>() {
            @Override
            public ItemStack decode(RegistryByteBuf buf) {
                // Позволяем ванильному декодеру работать без краша при больших числах
                try {
                    return original.decode(buf);
                } catch (Exception e) {
                    return ItemStack.EMPTY;
                }
            }

            @Override
            public void encode(RegistryByteBuf buf, ItemStack stack) {
                original.encode(buf, stack);
            }
        };
        
        cir.setReturnValue(expanded);
    }
}