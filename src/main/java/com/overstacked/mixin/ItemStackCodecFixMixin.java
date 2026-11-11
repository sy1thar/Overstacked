package com.overstacked.mixin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/** Preserves large stack counts through MAP_CODEC, CODEC, OPTIONAL_CODEC and validation. */
@Mixin(value = ItemStack.class, priority = 1500)
public abstract class ItemStackCodecFixMixin {

    @Shadow @Mutable
    public static MapCodec<ItemStack> MAP_CODEC;

    @Shadow @Mutable
    public static Codec<ItemStack> CODEC;

    @Shadow @Mutable
    public static Codec<ItemStack> OPTIONAL_CODEC;

    @Shadow
    private static DataResult<?> validateComponents(DataComponentMap components) { return null; }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void overstacked$replaceCodecs(CallbackInfo ci) {
        final MapCodec<ItemStack> originalMapCodec = MAP_CODEC;
        MAP_CODEC = overstacked$wrapMapCodec(originalMapCodec);
        CODEC = MAP_CODEC.codec();
        OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
                opt -> opt.orElse(ItemStack.EMPTY),
                stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack)
        );
    }

    @Unique
    private static MapCodec<ItemStack> overstacked$wrapMapCodec(MapCodec<ItemStack> original) {
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return original.keys(ops);
            }

            @Override
            public <T> DataResult<ItemStack> decode(DynamicOps<T> ops, MapLike<T> input) {
                T rawCount = input.get("count");
                int count = rawCount != null
                        ? ops.getNumberValue(rawCount).map(Number::intValue).result().orElse(1)
                        : 1;

                if (count <= 99) {
                    return original.decode(ops, input);
                }

                final T one = ops.createInt(1);
                MapLike<T> patchedInput = new MapLike<>() {
                    @Override
                    public T get(T key) {
                        if (ops.getStringValue(key).result().filter("count"::equals).isPresent()) {
                            return one;
                        }
                        return input.get(key);
                    }

                    @Override
                    public T get(String key) {
                        if ("count".equals(key)) {
                            return one;
                        }
                        return input.get(key);
                    }

                    @Override
                    public Stream<Pair<T, T>> entries() {
                        return input.entries().map(pair ->
                                ops.getStringValue(pair.getFirst()).result().filter("count"::equals).isPresent()
                                        ? Pair.of(pair.getFirst(), one)
                                        : pair
                        );
                    }
                };

                return original.decode(ops, patchedInput).map(stack -> {
                    stack.setCount(count);
                    return stack;
                });
            }

            @Override
            public <T> RecordBuilder<T> encode(ItemStack input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                int originalCount = input.getCount();
                if (originalCount <= 99) {
                    return original.encode(input, ops, prefix);
                }

                ItemStack copy = input.copyWithCount(1);
                final T countVal = ops.createInt(originalCount);
                RecordBuilder<T> wrappedBuilder = new RecordBuilder<>() {
                    private boolean isCountKey(T key) {
                        return ops.getStringValue(key).result().filter("count"::equals).isPresent();
                    }

                    @Override
                    public DynamicOps<T> ops() {
                        return prefix.ops();
                    }

                    @Override
                    public RecordBuilder<T> add(T key, T value) {
                        prefix.add(key, isCountKey(key) ? countVal : value);
                        return this;
                    }

                    @Override
                    public RecordBuilder<T> add(T key, DataResult<T> value) {
                        prefix.add(key, isCountKey(key) ? DataResult.success(countVal) : value);
                        return this;
                    }

                    @Override
                    public RecordBuilder<T> add(DataResult<T> key, DataResult<T> value) {
                        boolean isCount = key.result().map(this::isCountKey).orElse(false);
                        prefix.add(key, isCount ? DataResult.success(countVal) : value);
                        return this;
                    }

                    @Override
                    public RecordBuilder<T> add(String key, T value) {
                        prefix.add(key, "count".equals(key) ? countVal : value);
                        return this;
                    }

                    @Override
                    public RecordBuilder<T> add(String key, DataResult<T> value) {
                        prefix.add(key, "count".equals(key) ? DataResult.success(countVal) : value);
                        return this;
                    }

                    @Override
                    public RecordBuilder<T> withErrorsFrom(DataResult<?> result) {
                        prefix.withErrorsFrom(result);
                        return this;
                    }

                    @Override
                    public RecordBuilder<T> setLifecycle(Lifecycle lifecycle) {
                        prefix.setLifecycle(lifecycle);
                        return this;
                    }

                    @Override
                    public RecordBuilder<T> mapError(UnaryOperator<String> onError) {
                        prefix.mapError(onError);
                        return this;
                    }

                    @Override
                    public DataResult<T> build(T prefixVal) {
                        return prefix.build(prefixVal);
                    }
                };

                return original.encode(copy, ops, wrappedBuilder);
            }

            @Override
            public String toString() {
                return "OverstackedMapCodec[" + original + "]";
            }
        };
    }

    @Inject(method = "validateStrict", at = @At("HEAD"), cancellable = true)
    private static void overstacked$bypassCountValidation(
            ItemStack stack, CallbackInfoReturnable<DataResult<ItemStack>> cir) {
        DataResult<?> componentResult = validateComponents(stack.getComponents());
        if (componentResult.isError()) {
            cir.setReturnValue(componentResult.map(unit -> stack));
            return;
        }
        cir.setReturnValue(DataResult.success(stack));
    }
}
