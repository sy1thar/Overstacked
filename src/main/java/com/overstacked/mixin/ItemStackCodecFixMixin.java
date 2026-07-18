package com.overstacked.mixin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.overstacked.config.OverstackedConfig;

/**
 * Fixes item deletion caused by vanilla Codec validation on large stacks.
 *
 * ROOT CAUSE:
 * 1. ItemStack.CODEC encodes count with Codecs.rangedInt(1, 99).
 *    Any stack with count > 99 fails encode/decode → item silently deleted.
 * 2. ItemStack.validate() checks getCount() > getMaxCount() and returns
 *    DataResult.error → VALIDATED_CODEC rejects the stack.
 *
 * FIX:
 * A. Replace CODEC at class init with a wrapper that intercepts encode/decode.
 *    On decode: read raw count from NBT, set count=1 in NBT copy, decode,
 *    then restore real count on the resulting ItemStack.
 *    On encode: set count=1, encode via original, then write real count to output.
 * B. Bypass validate() count check (keep component validation intact).
 */
@Mixin(value = ItemStack.class, priority = 1500)
public abstract class ItemStackCodecFixMixin {

    @Shadow @Mutable
    private static Codec<ItemStack> CODEC;

    @Shadow @Mutable
    private static Codec<ItemStack> VALIDATED_CODEC;

    @Shadow
    private static Logger LOGGER;

    @Shadow
    private static DataResult<ItemStack> validate(ItemStack stack) { return null; }

    @Shadow
    public static DataResult<net.minecraft.util.Unit> validateComponents(
            net.minecraft.component.ComponentMap components) { return null; }

    /**
     * Phase 1: Replace CODEC after vanilla static init completes.
     * The original CODEC uses rangedInt(1,99) for count — we wrap it with
     * a codec that intercepts encode/decode to handle counts up to 1 billion.
     */
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void overstacked$replaceCodec(CallbackInfo ci) {
        final Codec<ItemStack> original = CODEC;

        CODEC = new Codec<ItemStack>() {
            @Override
            public <V> DataResult<V> encode(ItemStack input, DynamicOps<V> ops, V prefix) {
                int originalCount = input.getCount();

                if (originalCount <= 99 || !(ops instanceof NbtOps)) {
                    return original.encode(input, ops, prefix);
                }

                input.setCount(1);
                DataResult<V> result = original.encode(input, ops, prefix);
                input.setCount(originalCount);

                return result.map(encoded -> {
                    if (encoded instanceof NbtCompound compound) {
                        compound.putInt("count", originalCount);
                    }
                    return encoded;
                });
            }

            @Override
            public <V> DataResult<Pair<ItemStack, V>> decode(DynamicOps<V> ops, V input) {
                int count = overstacked$extractCount(ops, input);

                if (count <= 99) {
                    return original.decode(ops, input);
                }

                V fixed = overstacked$withCount(ops, input, 1);
                if (fixed == null) {
                    return original.decode(ops, input);
                }

                return original.decode(ops, fixed).map(pair -> {
                    pair.getFirst().setCount(count);
                    return pair;
                });
            }

            @Override
            public String toString() {
                return "OverstackedCODEC[" + original + "]";
            }
        };

        VALIDATED_CODEC = CODEC.validate(ItemStackCodecFixMixin::validate);
    }

    /**
     * Phase 2: Bypass validate() count check.
     * validate() does: if (getCount() > getMaxCount()) return error
     * We keep component validation (MAX_DAMAGE etc.) but skip count validation
     * since our config allows large counts.
     */
    @Inject(method = "validate", at = @At("HEAD"), cancellable = true)
    private static void overstacked$bypassCountValidation(
            ItemStack stack, CallbackInfoReturnable<DataResult<ItemStack>> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();
        if (!config.isLoaded()) return;

        DataResult<net.minecraft.util.Unit> componentResult =
                validateComponents(stack.getComponents());
        if (componentResult.isError()) {
            cir.setReturnValue(componentResult.map(unit -> stack));
        }
        cir.setReturnValue(DataResult.success(stack));
    }

    /**
     * Phase 3: Safety net on encode(Lookup, NbtElement).
     * If code calls stack.encode() directly instead of CODEC.encode(),
     * this intercepts before any codec runs and handles large counts.
     */
    @Inject(method = "encode(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;Lnet/minecraft/nbt/NbtElement;)Lnet/minecraft/nbt/NbtElement;",
            at = @At("HEAD"), cancellable = true)
    private void overstacked$encodeTwoArg(
            RegistryWrapper.WrapperLookup registries, NbtElement prefix,
            CallbackInfoReturnable<NbtElement> cir) {
        overstacked$handleEncode(registries, cir);
    }

    @Inject(method = "encode(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/nbt/NbtElement;",
            at = @At("HEAD"), cancellable = true)
    private void overstacked$encodeOneArg(
            RegistryWrapper.WrapperLookup registries,
            CallbackInfoReturnable<NbtElement> cir) {
        overstacked$handleEncode(registries, cir);
    }

    @Unique
    private void overstacked$handleEncode(
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

    @Unique
    @SuppressWarnings("unchecked")
    private static <V> int overstacked$extractCount(DynamicOps<V> ops, V input) {
        if (input instanceof NbtCompound compound) {
            if (compound.contains("count", 99)) {
                return compound.getInt("count");
            }
        }
        return 1;
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static <V> V overstacked$withCount(DynamicOps<V> ops, V input, int count) {
        if (input instanceof NbtCompound compound) {
            NbtCompound copy = compound.copy();
            copy.putInt("count", count);
            return (V) copy;
        }
        return null;
    }
}
