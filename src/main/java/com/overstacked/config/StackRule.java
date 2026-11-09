package com.overstacked.config;

import net.minecraft.core.component.DataComponents;
import net.minecraft.item.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;

public class StackRule {
    public static int defaultMaxStackSize = 1000;

    /**
     * Главный фильтр мода. Если предмет - оружие, броня или инструмент,
     * он жестко фиксируется на лимите 1, даже если на нем нет износа.
     */
    public static boolean isUnstackable(ItemStack stack) {
        Item item = stack.getItem();

        // 1. Изоляция по классам экипировки
        if (item instanceof SwordItem || item instanceof TieredItem || 
            item instanceof ArmorItem || item instanceof ProjectileWeaponItem || 
            item instanceof TridentItem || item instanceof ShieldItem || 
            item instanceof FishingRodItem || item instanceof ShearsItem ||
            item instanceof FlintAndSteelItem || item instanceof MaceItem) {
            return true;
        }

        // 2. Изоляция по компоненту прочности
        if (stack.has(DataComponents.MAX_DAMAGE) || stack.isDamageableItem()) {
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