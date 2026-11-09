package com.overstacked.client;

import com.overstacked.config.OverstackedConfig;
import com.overstacked.network.MenuSubmitPayload;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class OverstackedConfigScreen {

    private OverstackedConfigScreen() {}

    public static Screen create(Screen parent) {
        OverstackedConfig config = OverstackedConfig.getInstance();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("key.overstacked.config"))
                .setSavingRunnable(() -> {
                    config.reloadDiskRules();
                    config.saveToDisk();
                    syncToServer(config);
                });

        ConfigEntryBuilder eb = builder.entryBuilder();

        /* ========== Category 1: General Settings ========== */
        ConfigCategory general = builder.getOrCreateCategory(
                Component.translatable("category.overstacked.general"));

        general.addEntry(eb.startIntField(
                        Component.translatable("option.overstacked.defaultMaxStackSize"), config.getDefaultMaxStackSize())
                .setDefaultValue(1_000)
                .setMin(1).setMax(1_000_000_000)
                .setTooltip(Component.translatable("tooltip.overstacked.defaultMaxStackSize"))
                .setSaveConsumer(config::setDefaultMaxStackSize)
                .build());

        /* ========== Category 2: Unstackable Item Overrides ========== */
        ConfigCategory overrides = builder.getOrCreateCategory(
                Component.translatable("category.overstacked.overrides"));

        SubCategoryBuilder sub = eb.startSubCategory(
                Component.translatable("category.overstacked.itemToggles"));
        sub.setExpanded(true);

        sub.add(eb.startBooleanToggle(
                        Component.translatable("option.overstacked.stackEnchantedBooks"), config.isStackEnchantedBooks())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.overstacked.stackEnchantedBooks"))
                .setSaveConsumer(config::setStackEnchantedBooks)
                .build());

        sub.add(eb.startBooleanToggle(
                        Component.translatable("option.overstacked.stackEnderPearls"), config.isStackEnderPearlsSnowballs())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.overstacked.stackEnderPearls"))
                .setSaveConsumer(config::setStackEnderPearlsSnowballs)
                .build());

        sub.add(eb.startBooleanToggle(
                        Component.translatable("option.overstacked.stackBucketsPotions"), config.isStackBucketsPotions())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.overstacked.stackBucketsPotions"))
                .setSaveConsumer(config::setStackBucketsPotions)
                .build());

        sub.add(eb.startBooleanToggle(
                        Component.translatable("option.overstacked.stackTotems"), config.isStackTotems())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("tooltip.overstacked.stackTotems"))
                .setSaveConsumer(config::setStackTotems)
                .build());

        sub.add(eb.startBooleanToggle(
                        Component.translatable("option.overstacked.stackToolsArmor"), config.isStackToolsArmor())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("tooltip.overstacked.stackToolsArmor"))
                .setSaveConsumer(config::setStackToolsArmor)
                .build());

        overrides.addEntry(sub.build());

        /* ========== Category 3: Advanced Rules & Disk Reloading ========== */
        ConfigCategory advanced = builder.getOrCreateCategory(
                Component.translatable("category.overstacked.advanced"));

        advanced.addEntry(eb.startTextDescription(
                Component.translatable("option.overstacked.reloadInfo"))
                .build());

        int itemCount = config.getItemOverrideCount();
        int tagCount = config.getTagOverrideCount();
        advanced.addEntry(eb.startTextDescription(
                Component.translatable("option.overstacked.overridesInfo",
                        String.valueOf(itemCount), String.valueOf(tagCount)))
                .build());

        return builder.build();
    }

    private static void syncToServer(OverstackedConfig config) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null) return;
        if (client.hasSingleplayerServer()) return;

        MenuSubmitPayload payload = new MenuSubmitPayload(
                config.getDefaultMaxStackSize(),
                config.isStackEnchantedBooks(),
                config.isStackEnderPearlsSnowballs(),
                config.isStackBucketsPotions(),
                config.isStackTotems(),
                config.isStackToolsArmor(),
                new java.util.LinkedHashMap<>(config.getItemOverrides()),
                new java.util.LinkedHashMap<>(config.getTagOverrides()));
        ClientPlayNetworking.send(payload);
    }
}
