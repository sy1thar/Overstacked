package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Containers.class)
public abstract class ItemScattererMixin {

    @Inject(method = "dropContents(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/Container;)V",
            at = @At("HEAD"), cancellable = true)
    private static void overstacked$preserveLargeStacks(
            Level world, double x, double y, double z,
            Container inventory, CallbackInfo ci) {

        boolean hasLargeStack = false;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getCount() > OverstackedConfig.getVanillaMaxCount(stack.getItem())) {
                hasLargeStack = true;
                break;
            }
        }
        if (!hasLargeStack) return;

        RandomSource random = world.getRandom();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;

            ItemEntity entity = new ItemEntity(world, x, y, z, stack);
            entity.setDeltaMovement(
                    random.triangle(0.0, 0.11485000171139836),
                    random.triangle(0.2, 0.11485000171139836),
                    random.triangle(0.0, 0.11485000171139836));
            world.addFreshEntity(entity);
        }

        ci.cancel();
    }
}
