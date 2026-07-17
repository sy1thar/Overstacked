package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public interface InventoryMaxCountMixin {

    @Inject(method = "getMaxCountPerStack", at = @At("RETURN"), cancellable = true)
    private void overstacked$overrideMaxCountPerStack(CallbackInfoReturnable<Integer> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();
        if (config.isLoaded()) {
            cir.setReturnValue(config.getDefaultMaxStackSize());
        }
    }
}
