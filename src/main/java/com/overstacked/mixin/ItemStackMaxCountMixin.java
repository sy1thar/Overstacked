package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides {@link ItemStack#getMaxCount()} to return our config value.
 * NO isLoaded() guard — returns hardcoded default (1B) before config loads
 * so that validate() and capCount() never reject large stacks during bootstrap.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMaxCountMixin {

    private static final int OVERSTACKED_DEFAULT = 1_000_000_000;

    @Inject(method = "getMaxCount", at = @At("RETURN"), cancellable = true)
    private void overstacked$overrideMaxCount(CallbackInfoReturnable<Integer> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();

        int max;
        if (config.isLoaded()) {
            ItemStack self = (ItemStack) (Object) this;
            max = config.getMaxStackSize(self.getItem());
        } else {
            max = OVERSTACKED_DEFAULT;
        }

        if (max != cir.getReturnValue()) {
            cir.setReturnValue(max);
        }
    }
}
