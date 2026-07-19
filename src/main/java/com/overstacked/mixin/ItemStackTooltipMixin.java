package com.overstacked.mixin;

import com.overstacked.config.OverstackedConfig;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void overstacked$addExactCount(Item.TooltipContext context, PlayerEntity player, TooltipType tooltipType, CallbackInfoReturnable<List<Text>> cir) {
        OverstackedConfig config = OverstackedConfig.getInstance();

        ItemStack self = (ItemStack) (Object) this;
        int count = self.getCount();
        if (count <= 1) return;

        int vanillaMax = OverstackedConfig.getVanillaMaxCount(self.getItem());
        int ourMax = config.getMaxStackSize(self.getItem());
        if (ourMax <= vanillaMax) return;

        cir.getReturnValue().add(Text.literal("§7x" + String.format("%,d", count)));
    }
}
