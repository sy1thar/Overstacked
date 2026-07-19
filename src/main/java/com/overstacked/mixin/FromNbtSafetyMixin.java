package com.overstacked.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Safety net for corrupted NBT data that produces ItemStack with count < 1.
 *
 * fromNbtOrEmpty calls fromNbt().orElse(EMPTY). If our wrapped codec somehow
 * produces an ItemStack with count <= 0 (e.g. negative count in NBT, or data
 * corruption), this mixin fixes it before any game logic sees it.
 */
@Mixin(ItemStack.class)
public abstract class FromNbtSafetyMixin {

    @Inject(method = "fromNbtOrEmpty(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/item/ItemStack;",
            at = @At("RETURN"))
    private static void overstacked$validateCount(
            RegistryWrapper.WrapperLookup registries, NbtCompound nbt,
            CallbackInfoReturnable<ItemStack> cir) {

        ItemStack result = cir.getReturnValue();
        if (result.isEmpty()) return;

        if (result.getCount() < 1) {
            result.setCount(1);
        }
    }
}
