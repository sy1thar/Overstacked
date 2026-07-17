package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides {@link Item#getMaxCount()} to return the value from
 * our XML/JSON configuration instead of the vanilla default (64).
 */
@Mixin(Item.class)
public abstract class ItemMaxStackSizeMixin {

    @Inject(method = "getMaxCount", at = @At("RETURN"), cancellable = true)
    private void overstacked$overrideMaxCount(CallbackInfoReturnable<Integer> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();
        if (!config.isLoaded()) return;

        Item self = (Item) (Object) this;
        cir.setReturnValue(config.getMaxStackSize(self));
    }
}
