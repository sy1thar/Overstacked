package com.overstacked.mixin;

import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Raises inventory capacity while the vanilla stack-aware overload applies item limits.
 */
@Mixin(Container.class)
public interface InventoryMaxCountMixin {

    @Inject(method = "getMaxCountPerStack()I", at = @At("RETURN"), cancellable = true)
    private void overstacked$overrideMaxCountPerStack(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(1_000_000_000);
    }
}
