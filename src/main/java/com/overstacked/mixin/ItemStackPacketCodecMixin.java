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

    @Inject(method = "validatedStreamCodec", at = @At("RETURN"), cancellable = true)
    private static void overstacked$expandPacketCodec(
            StreamCodec<RegistryFriendlyByteBuf, ItemStack> codec,
            CallbackInfoReturnable<StreamCodec<RegistryFriendlyByteBuf, ItemStack>> cir) {
        StreamCodec<RegistryFriendlyByteBuf, ItemStack> original = cir.getReturnValue();

        StreamCodec<RegistryFriendlyByteBuf, ItemStack> expanded = new StreamCodec<>() {
            @Override
            public ItemStack decode(RegistryFriendlyByteBuf buf) {
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
