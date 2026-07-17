package com.overstacked.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackEncodeMixin {

    @Shadow
    private static Codec<ItemStack> CODEC;

    @Inject(method = "encode(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;Lnet/minecraft/nbt/NbtElement;)Lnet/minecraft/nbt/NbtElement;",
            at = @At("HEAD"), cancellable = true)
    private void overstacked$bypassCodecValidationTwoArg(
            RegistryWrapper.WrapperLookup registries, NbtElement prefix,
            CallbackInfoReturnable<NbtElement> cir) {
        overstacked$bypassCodecValidation(registries, cir);
    }

    @Inject(method = "encode(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/nbt/NbtElement;",
            at = @At("HEAD"), cancellable = true)
    private void overstacked$bypassCodecValidationOneArg(
            RegistryWrapper.WrapperLookup registries,
            CallbackInfoReturnable<NbtElement> cir) {
        overstacked$bypassCodecValidation(registries, cir);
    }

    @Unique
    private void overstacked$bypassCodecValidation(
            RegistryWrapper.WrapperLookup registries,
            CallbackInfoReturnable<NbtElement> cir) {

        ItemStack self = (ItemStack) (Object) this;
        int originalCount = self.getCount();

        if (originalCount <= 99) {
            return;
        }

        self.setCount(1);
        DataResult<NbtElement> result = CODEC
                .encodeStart(RegistryOps.of(NbtOps.INSTANCE, registries), self);
        self.setCount(originalCount);

        if (result.isError()) {
            return;
        }

        NbtElement encoded = result.result().get();
        if (encoded instanceof NbtCompound compound) {
            compound.putInt("count", originalCount);
            cir.setReturnValue(compound);
        } else {
            cir.setReturnValue(encoded);
        }
    }
}
