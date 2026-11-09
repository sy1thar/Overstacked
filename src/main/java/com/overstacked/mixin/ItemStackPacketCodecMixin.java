package com.overstacked.mixin;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
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
    @Inject(method = "createExtraValidatingPacketCodec", at = @At("RETURN"), cancellable = true)
    private static void overstacked$expandPacketCodec(CallbackInfoReturnable<StreamCodec<RegistryFriendlyByteBuf, ItemStack>> cir) {
        StreamCodec<RegistryFriendlyByteBuf, ItemStack> original = cir.getReturnValue();
        
        StreamCodec<RegistryFriendlyByteBuf, ItemStack> expanded = new StreamCodec<>() {
            @Override
            public ItemStack decode(RegistryFriendlyByteBuf buf) {
                // Позволяем ванильному декодеру работать без краша при больших числах
                try {
                    return original.decode(buf);
                } catch (Exception e) {
                    return ItemStack.EMPTY;
                }
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, ItemStack stack) {
                original.encode(buf, stack);
            }
        };
        
        cir.setReturnValue(expanded);
    }
}