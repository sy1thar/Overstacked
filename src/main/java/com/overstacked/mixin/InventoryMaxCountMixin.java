package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides {@link Inventory#getMaxCountPerStack()} to return our config value.
 * NO isLoaded() guard — returns hardcoded default (1B) before config loads.
 */
@Mixin(Inventory.class)
public interface InventoryMaxCountMixin {

    int OVERSTACKED_DEFAULT = 1_000_000_000;

    @Inject(method = "getMaxCountPerStack", at = @At("RETURN"), cancellable = true)
    private void overstacked$overrideMaxCountPerStack(CallbackInfoReturnable<Integer> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();
        if (config.isLoaded()) {
            cir.setReturnValue(config.getDefaultMaxStackSize());
        } else {
            cir.setReturnValue(OVERSTACKED_DEFAULT);
        }
    }
}
