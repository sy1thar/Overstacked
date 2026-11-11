package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMaxCountMixin {

    @Inject(method = "getMaxStackSize()I", at = @At("RETURN"), cancellable = true)
    private void overstacked$overrideSlotMax(CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() >= 64) {
            OverstackedConfig config = OverstackedConfig.getInstance();
            int desired = config.isLoaded() ? config.getDefaultMaxStackSize() : 1_000_000_000;
            if (desired > cir.getReturnValueI()) {
                cir.setReturnValue(desired);
            }
        }
    }

    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"), cancellable = true)
    private void overstacked$overrideSlotStackMax(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.isEmpty()) return;
        OverstackedConfig config = OverstackedConfig.getInstance();
        int desired = config.isLoaded() ? config.getMaxStackSize(stack.getItem()) : 1_000_000_000;
        Slot self = (Slot) (Object) this;
        int slotCap = self.getMaxStackSize();
        int effective = Math.min(slotCap, desired);
        if (effective > cir.getReturnValueI() && cir.getReturnValueI() >= OverstackedConfig.getVanillaMaxCount(stack.getItem())) {
            cir.setReturnValue(effective);
        }
    }
}
