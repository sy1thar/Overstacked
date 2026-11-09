package com.overstacked.mixin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Preserves large stack counts through both NBT and JSON codecs. */
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
            net.minecraft.core.component.DataComponentMap components) { return null; }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void overstacked$replaceCodecs(CallbackInfo ci) {
        final Codec<ItemStack> originalCodec = CODEC;

        CODEC = overstacked$wrapCodec(originalCodec, "CODEC");
        // UNCOUNTED_CODEC intentionally omits count and retains vanilla semantics.

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

                ItemStack copy = input.copyWithCount(1);
                return original.encode(copy, ops, prefix)
                        .map(encoded -> ops.set(encoded, "count", ops.createInt(originalCount)));
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
    private static <V> int overstacked$extractCount(DynamicOps<V> ops, V input) {
        return ops.get(input, "count").flatMap(ops::getNumberValue)
                .map(Number::intValue).result().orElse(1);
    }

    @Unique
    private static <V> V overstacked$withCount(DynamicOps<V> ops, V input, int count) {
        return ops.set(input, "count", ops.createInt(count));
    }
}
