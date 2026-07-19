package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemScatterer.class)
public abstract class ItemScattererMixin {

    @Inject(method = "spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/inventory/Inventory;)V",
            at = @At("HEAD"), cancellable = true)
    private static void overstacked$preserveLargeStacks(
            World world, double x, double y, double z,
            Inventory inventory, CallbackInfo ci) {

        OverstackedConfig config = OverstackedConfig.getInstance();

        boolean hasLargeStack = false;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && stack.getCount() > OverstackedConfig.getVanillaMaxCount(stack.getItem())) {
                hasLargeStack = true;
                break;
            }
        }
        if (!hasLargeStack) return;

        Random random = world.random;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;

            ItemEntity entity = new ItemEntity(world, x, y, z, stack);
            entity.setVelocity(
                    random.nextTriangular(0.0, 0.11485000171139836),
                    random.nextTriangular(0.2, 0.11485000171139836),
                    random.nextTriangular(0.0, 0.11485000171139836));
            world.spawnEntity(entity);
        }

        ci.cancel();
    }
}
