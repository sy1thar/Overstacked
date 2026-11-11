package com.overstacked.mixin;

import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
public interface InventoryMaxCountMixin {

    @Inject(method = "getMaxStackSize()I", at = @At("RETURN"), cancellable = true)
    private void overstacked$overrideMaxCountPerStack(CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() >= 64) {
            cir.setReturnValue(1_000_000_000);
        }
    }
}
