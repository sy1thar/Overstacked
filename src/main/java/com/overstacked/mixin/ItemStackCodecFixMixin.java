package com.overstacked.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.overstacked.util.ItemStackCodecHelper;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

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
        MAP_CODEC = ItemStackCodecHelper.wrapMapCodec(MAP_CODEC);
        CODEC = MAP_CODEC.codec();
        OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
                opt -> opt.orElse(ItemStack.EMPTY),
                stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack)
        );
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
