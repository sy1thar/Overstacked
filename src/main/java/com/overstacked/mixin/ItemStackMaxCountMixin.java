package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMaxCountMixin {

    @Inject(method = "getMaxCount", at = @At("RETURN"), cancellable = true)
    private void overstacked$overrideMaxCount(CallbackInfoReturnable<Integer> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();
        if (!config.isLoaded()) return;

        ItemStack self = (ItemStack) (Object) this;
        Item item = self.getItem();

        int max = config.getMaxStackSize(item);
        if (max != cir.getReturnValue()) {
            cir.setReturnValue(max);
        }
    }
}
