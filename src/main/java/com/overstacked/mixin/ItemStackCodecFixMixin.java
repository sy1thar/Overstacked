package com.overstacked.mixin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes item deletion caused by vanilla Codec validation on large stacks.
 *
 * ItemStack has 4 static Codec fields:
 *   CODEC                   — 2-field: {id, components}. NO count. Used by fromNbt() and encode().
 *   UNCOUNTED_CODEC         — 3-field: {id, count(rangedInt 1-99), components}. Has count.
 *   VALIDATED_CODEC         — CODEC.validate(ItemStack::validate)
 *   VALIDATED_UNCOUNTED_CODEC — UNCOUNTED_CODEC.validate(ItemStack::validate)
 *
 * validate() checks: getCount() > getMaxCount() → DataResult.error → item deleted.
 *
 * FIX:
 * 1. Replace all 4 codecs at <clinit> RETURN with wrappers that handle count > 99.
 *    The wrapper passes `prefix` through to original.encode(), preserving "Slot" bytes
 *    in PlayerInventory.writeNbt.
 * 2. Bypass validate() count check entirely (keep component validation).
 */
@Mixin(value = ItemStack.class, priority = 1500)
public abstract class ItemStackCodecFixMixin {

    @Shadow @Mutable
    private static Codec<ItemStack> CODEC;

    @Shadow @Mutable
    private static Codec<ItemStack> UNCOUNTED_CODEC;

    @Shadow @Mutable
    private static Codec<ItemStack> VALIDATED_CODEC;

    @Shadow @Mutable
    private static Codec<ItemStack> VALIDATED_UNCOUNTED_CODEC;

    @Shadow
    private static Logger LOGGER;

    @Shadow
    private static DataResult<ItemStack> validate(ItemStack stack) { return null; }

    @Shadow
    public static DataResult<net.minecraft.util.Unit> validateComponents(
            net.minecraft.component.ComponentMap components) { return null; }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void overstacked$replaceCodecs(CallbackInfo ci) {
        final Codec<ItemStack> originalCodec = CODEC;
        final Codec<ItemStack> originalUncounted = UNCOUNTED_CODEC;

        CODEC = overstacked$wrapCodec(originalCodec, "CODEC");
        UNCOUNTED_CODEC = overstacked$wrapCodec(originalUncounted, "UNCOUNTED_CODEC");

        VALIDATED_CODEC = CODEC.validate(ItemStackCodecFixMixin::validate);
        VALIDATED_UNCOUNTED_CODEC = UNCOUNTED_CODEC.validate(ItemStackCodecFixMixin::validate);
    }

    @Unique
    private static Codec<ItemStack> overstacked$wrapCodec(Codec<ItemStack> original, String name) {
        return new Codec<ItemStack>() {
            @Override
            public <V> DataResult<V> encode(ItemStack input, DynamicOps<V> ops, V prefix) {
                int originalCount = input.getCount();

                if (originalCount <= 99) {
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
                int count = Math.max(1, overstacked$extractCount(ops, input));

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
                return "OverstackedCodec[" + name + "=" + original + "]";
            }
        };
    }

    @Inject(method = "validate", at = @At("HEAD"), cancellable = true)
    private static void overstacked$bypassCountValidation(
            ItemStack stack, CallbackInfoReturnable<DataResult<ItemStack>> cir) {
        DataResult<net.minecraft.util.Unit> componentResult =
                validateComponents(stack.getComponents());
        if (componentResult.isError()) {
            cir.setReturnValue(componentResult.map(unit -> stack));
            return;
        }
        cir.setReturnValue(DataResult.success(stack));
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
