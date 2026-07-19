package com.overstacked.config;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;

public class StackRule {
    public static int defaultMaxStackSize = 1000;

    /**
     * Главный фильтр мода. Если предмет - оружие, броня или инструмент,
     * он жестко фиксируется на лимите 1, даже если на нем нет износа.
     */
    public static boolean isUnstackable(ItemStack stack) {
        Item item = stack.getItem();

        // 1. Изоляция по классам экипировки
        if (item instanceof SwordItem || item instanceof ToolItem || 
            item instanceof ArmorItem || item instanceof RangedWeaponItem || 
            item instanceof TridentItem || item instanceof ShieldItem || 
            item instanceof FishingRodItem || item instanceof ShearsItem ||
            item instanceof FlintAndSteelItem || item instanceof MaceItem) {
            return true;
        }

        // 2. Изоляция по компоненту прочности
        if (stack.contains(DataComponentTypes.MAX_DAMAGE) || stack.isDamageable()) {
            return true;
        }

        return false;
    }

    public static int getMaxCount(ItemStack stack, int originalMax) {
        if (isUnstackable(stack)) {
            return 1;
        }
        return originalMax > 1 ? defaultMaxStackSize : originalMax;
    }
}