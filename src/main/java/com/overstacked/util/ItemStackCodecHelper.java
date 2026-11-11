package com.overstacked.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Helper holding the non-mixin MapCodec/MapLike/RecordBuilder wrappers for Overstacked.
 * Kept outside @Mixin classes so older SpongePowered Mixin versions (e.g. 0.17.0 in Fabric Loader 0.18.x)
 * never reject nested inner classes generated inside a mixin.
 */
public final class ItemStackCodecHelper {

    private ItemStackCodecHelper() {}

    public static MapCodec<ItemStack> wrapMapCodec(MapCodec<ItemStack> original) {
        return new OverstackedMapCodec(original);
    }

    private static final class OverstackedMapCodec extends MapCodec<ItemStack> {
        private final MapCodec<ItemStack> original;

        OverstackedMapCodec(MapCodec<ItemStack> original) {
            this.original = original;
        }

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

            return original.decode(ops, new PatchedCountMapLike<>(ops, input)).map(stack -> {
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
            return original.encode(copy, ops, new OverstackedCountRecordBuilder<>(ops, prefix, originalCount));
        }

        @Override
        public String toString() {
            return "OverstackedMapCodec[" + original + "]";
        }
    }

    private static final class PatchedCountMapLike<T> implements MapLike<T> {
        private final DynamicOps<T> ops;
        private final MapLike<T> delegate;
        private final T one;

        PatchedCountMapLike(DynamicOps<T> ops, MapLike<T> delegate) {
            this.ops = ops;
            this.delegate = delegate;
            this.one = ops.createInt(1);
        }

        private boolean isCountKey(T key) {
            return ops.getStringValue(key).result().filter("count"::equals).isPresent();
        }

        @Override
        public T get(T key) {
            return isCountKey(key) ? one : delegate.get(key);
        }

        @Override
        public T get(String key) {
            return "count".equals(key) ? one : delegate.get(key);
        }

        @Override
        public Stream<Pair<T, T>> entries() {
            return delegate.entries().map(pair ->
                    isCountKey(pair.getFirst()) ? Pair.of(pair.getFirst(), one) : pair
            );
        }
    }

    private static final class OverstackedCountRecordBuilder<T> implements RecordBuilder<T> {
        private final DynamicOps<T> ops;
        private final RecordBuilder<T> delegate;
        private final T countVal;

        OverstackedCountRecordBuilder(DynamicOps<T> ops, RecordBuilder<T> delegate, int count) {
            this.ops = ops;
            this.delegate = delegate;
            this.countVal = ops.createInt(count);
        }

        private boolean isCountKey(T key) {
            return ops.getStringValue(key).result().filter("count"::equals).isPresent();
        }

        @Override
        public DynamicOps<T> ops() {
            return delegate.ops();
        }

        @Override
        public RecordBuilder<T> add(T key, T value) {
            delegate.add(key, isCountKey(key) ? countVal : value);
            return this;
        }

        @Override
        public RecordBuilder<T> add(T key, DataResult<T> value) {
            delegate.add(key, isCountKey(key) ? DataResult.success(countVal) : value);
            return this;
        }

        @Override
        public RecordBuilder<T> add(DataResult<T> key, DataResult<T> value) {
            boolean isCount = key.result().map(this::isCountKey).orElse(false);
            delegate.add(key, isCount ? DataResult.success(countVal) : value);
            return this;
        }

        @Override
        public RecordBuilder<T> add(String key, T value) {
            delegate.add(key, "count".equals(key) ? countVal : value);
            return this;
        }

        @Override
        public RecordBuilder<T> add(String key, DataResult<T> value) {
            delegate.add(key, "count".equals(key) ? DataResult.success(countVal) : value);
            return this;
        }

        @Override
        public RecordBuilder<T> withErrorsFrom(DataResult<?> result) {
            delegate.withErrorsFrom(result);
            return this;
        }

        @Override
        public RecordBuilder<T> setLifecycle(Lifecycle lifecycle) {
            delegate.setLifecycle(lifecycle);
            return this;
        }

        @Override
        public RecordBuilder<T> mapError(UnaryOperator<String> onError) {
            delegate.mapError(onError);
            return this;
        }

        @Override
        public DataResult<T> build(T prefix) {
            return delegate.build(prefix);
        }
    }
}
