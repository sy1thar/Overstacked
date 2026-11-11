package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInstance.class)
public interface ItemStackMaxCountMixin {

    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void overstacked$overrideMaxCount(CallbackInfoReturnable<Integer> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();

        int max;
        if (config.isLoaded()) {
            ItemInstance self = (ItemInstance) this;
            Item item = self.typeHolder().value();
            max = config.getMaxStackSize(item);
        } else {
            max = 1_000_000_000;
        }

        if (max != cir.getReturnValueI()) {
            cir.setReturnValue(max);
        }
    }
}
