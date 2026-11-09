package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMergeMixin {

    @Inject(method = "canMerge", at = @At("HEAD"), cancellable = true)
    private void overstacked$preventLargeStackMerge(CallbackInfoReturnable<Boolean> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();

        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getItem();
        int vanillaMax = OverstackedConfig.getVanillaMaxCount(stack.getItem());

        if (stack.getCount() > vanillaMax) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canMerge(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void overstacked$preventLargeStackMergeStatic(ItemStack a, ItemStack b, CallbackInfoReturnable<Boolean> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();

        int vanillaMax = OverstackedConfig.getVanillaMaxCount(a.getItem());

        if (a.getCount() > vanillaMax || b.getCount() > vanillaMax) {
            cir.setReturnValue(false);
        }
    }
}
