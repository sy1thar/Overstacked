package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMaxStackSizeMixin {

    @Unique
    private static final int OVERSTACKED_DEFAULT = 1_000_000_000;

    @Inject(method = "getDefaultMaxStackSize", at = @At("RETURN"), cancellable = true)
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
