package com.overstacked.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class FromNbtSafetyMixin {

    /**
     * Перехватываем чтение ItemStack из NBT.
     * Если ванильный кодек пытается удалить предмет из-за размера стака > 64,
     * мы принудительно восстанавливаем его данные и реальное количество.
     */
    @Inject(method = "fromNbtOrEmpty(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/item/ItemStack;", 
            at = @At("RETURN"), cancellable = true)
    private static void overstacked$safeFromNbt(HolderLookup.Provider lookup, CompoundTag nbt, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        
        // Если ваниль вернула пустой предмет, но в NBT есть реальные данные и id не пустой
        if (result.isEmpty() && nbt != null && nbt.contains("id", Tag.TAG_STRING)) {
            try {
                // Извлекаем количество напрямую, обходя ванильный лимит кодека
                int count = 1;
                if (nbt.contains("count", Tag.TAG_INT)) {
                    count = nbt.getInt("count");
                } else if (nbt.contains("Count", Tag.TAG_BYTE)) {
                    count = nbt.getByte("Count") & 255;
                }

                // Если количество больше 0, пытаемся спасти предмет
                if (count > 0) {
                    // Позволяем ванильному кодеку прочитать предмет с числом 1, чтобы обмануть валидацию
                    CompoundTag tempNbt = nbt.copy();
                    tempNbt.putInt("count", 1);
                    
                    ItemStack rescuedStack = ItemStack.parse(lookup, tempNbt).orElse(ItemStack.EMPTY);
                    if (!rescuedStack.isEmpty()) {
                        rescuedStack.setCount(count);
                        cir.setReturnValue(rescuedStack);
                    }
                }
            } catch (Exception e) {
                System.err.println("[Overstacked] Предотвращено повреждение NBT слота: " + e.getMessage());
            }
        }
    }
}