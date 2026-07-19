package com.overstacked.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
public class ItemStackCodecFixMixin {

    @ModifyVariable(method = "<clinit>", at = @At("STORE"), ordinal = 0)
    private static Codec<ItemStack> overstacked$removeCodecLimit(Codec<ItemStack> original) {
        // Используем правильный метод для Minecraft 1.21.1 вместо .orElse()
        return Codec.withAlternative(original, Codec.unit(ItemStack.EMPTY));
    }
}