package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void overstacked$addExactCount(Item.TooltipContext context, Player player, TooltipFlag tooltipType, CallbackInfoReturnable<List<Component>> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();

        ItemStack self = (ItemStack) (Object) this;
        int count = self.getCount();
        if (count <= 1) return;

        int vanillaMax = OverstackedConfig.getVanillaMaxCount(self.getItem());
        int ourMax = config.getMaxStackSize(self.getItem());
        if (ourMax <= vanillaMax) return;

        cir.getReturnValue().add(Component.literal("§7x" + String.format("%,d", count)));
    }
}
