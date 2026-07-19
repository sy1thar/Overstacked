package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides {@link Item#getMaxCount()} to return the value from
 * our configuration instead of the vanilla default (64).
 * NO isLoaded() guard — returns hardcoded default (1B) before config loads.
 */
@Mixin(Item.class)
public abstract class ItemMaxStackSizeMixin {

    private static final int OVERSTACKED_DEFAULT = 1_000_000_000;

    @Inject(method = "getMaxCount", at = @At("RETURN"), cancellable = true)
    private void overstacked$overrideMaxCount(CallbackInfoReturnable<Integer> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();

        int max;
        if (config.isLoaded()) {
            Item self = (Item) (Object) this;
            max = config.getMaxStackSize(self);
        } else {
            max = OVERSTACKED_DEFAULT;
        }

        cir.setReturnValue(max);
    }
}
