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

    @Inject(method = "isMergable", at = @At("HEAD"), cancellable = true)
    private void overstacked$preventLargeStackMerge(CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getItem();
        int vanillaMax = OverstackedConfig.getVanillaMaxCount(stack.getItem());

        if (stack.getCount() > vanillaMax) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "areMergable(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void overstacked$preventLargeStackMergeStatic(ItemStack a, ItemStack b, CallbackInfoReturnable<Boolean> cir) {
        int vanillaMax = OverstackedConfig.getVanillaMaxCount(a.getItem());

        if (a.getCount() > vanillaMax || b.getCount() > vanillaMax) {
            cir.setReturnValue(false);
        }
    }
}
