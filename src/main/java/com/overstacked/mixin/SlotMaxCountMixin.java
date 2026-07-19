package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMaxCountMixin {

    @Inject(method = "getMaxItemCount", at = @At("RETURN"), cancellable = true)
    private void overstacked$overrideMaxItemCount(CallbackInfoReturnable<Integer> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();

        Slot self = (Slot) (Object) this;
        ItemStack stack = self.getStack();

        int desired;
        if (!stack.isEmpty()) {
            desired = config.getMaxStackSize(stack.getItem());
        } else {
            desired = config.getDefaultMaxStackSize();
        }

        if (desired > cir.getReturnValue()) {
            cir.setReturnValue(desired);
        }
    }
}
