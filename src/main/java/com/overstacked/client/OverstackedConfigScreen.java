package com.overstacked.client;

import com.overstacked.config.OverstackedConfig;
import com.overstacked.network.MenuSubmitPayload;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class OverstackedConfigScreen {

    private OverstackedConfigScreen() {}

    public static Screen create(Screen parent) {
        OverstackedConfig config = OverstackedConfig.getInstance();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("key.overstacked.config"))
                .setSavingRunnable(() -> {
                    config.reloadDiskRules();
                    config.saveToDisk();
                    syncToServer(config);
                });

        ConfigEntryBuilder eb = builder.entryBuilder();

        /* ========== Category 1: General Settings ========== */
        ConfigCategory general = builder.getOrCreateCategory(
                Text.translatable("category.overstacked.general"));

        general.addEntry(eb.startIntField(
                        Text.translatable("option.overstacked.defaultMaxStackSize"), config.getDefaultMaxStackSize())
                .setDefaultValue(1_000)
                .setMin(1).setMax(1_000_000_000)
                .setTooltip(Text.translatable("tooltip.overstacked.defaultMaxStackSize"))
                .setSaveConsumer(config::setDefaultMaxStackSize)
                .build());

        /* ========== Category 2: Unstackable Item Overrides ========== */
        ConfigCategory overrides = builder.getOrCreateCategory(
                Text.translatable("category.overstacked.overrides"));

        SubCategoryBuilder sub = eb.startSubCategory(
                Text.translatable("category.overstacked.itemToggles"));
        sub.setExpanded(true);

        sub.add(eb.startBooleanToggle(
                        Text.translatable("option.overstacked.stackEnchantedBooks"), config.isStackEnchantedBooks())
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.overstacked.stackEnchantedBooks"))
                .setSaveConsumer(config::setStackEnchantedBooks)
                .build());

        sub.add(eb.startBooleanToggle(
                        Text.translatable("option.overstacked.stackEnderPearls"), config.isStackEnderPearlsSnowballs())
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.overstacked.stackEnderPearls"))
                .setSaveConsumer(config::setStackEnderPearlsSnowballs)
                .build());

        sub.add(eb.startBooleanToggle(
                        Text.translatable("option.overstacked.stackBucketsPotions"), config.isStackBucketsPotions())
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.overstacked.stackBucketsPotions"))
                .setSaveConsumer(config::setStackBucketsPotions)
                .build());

        sub.add(eb.startBooleanToggle(
                        Text.translatable("option.overstacked.stackTotems"), config.isStackTotems())
                .setDefaultValue(false)
                .setTooltip(Text.translatable("tooltip.overstacked.stackTotems"))
                .setSaveConsumer(config::setStackTotems)
                .build());

        sub.add(eb.startBooleanToggle(
                        Text.translatable("option.overstacked.stackToolsArmor"), config.isStackToolsArmor())
                .setDefaultValue(false)
                .setTooltip(Text.translatable("tooltip.overstacked.stackToolsArmor"))
                .setSaveConsumer(config::setStackToolsArmor)
                .build());

        overrides.addEntry(sub.build());

        /* ========== Category 3: Advanced Rules & Disk Reloading ========== */
        ConfigCategory advanced = builder.getOrCreateCategory(
                Text.translatable("category.overstacked.advanced"));

        advanced.addEntry(eb.startTextDescription(
                Text.translatable("option.overstacked.reloadInfo"))
                .build());

        int itemCount = config.getItemOverrideCount();
        int tagCount = config.getTagOverrideCount();
        advanced.addEntry(eb.startTextDescription(
                Text.translatable("option.overstacked.overridesInfo",
                        String.valueOf(itemCount), String.valueOf(tagCount)))
                .build());

        return builder.build();
    }

    private static void syncToServer(OverstackedConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null) return;
        if (client.isIntegratedServerRunning()) return;

        MenuSubmitPayload payload = new MenuSubmitPayload(
                config.getDefaultMaxStackSize(),
                config.isStackEnchantedBooks(),
                config.isStackEnderPearlsSnowballs(),
                config.isStackBucketsPotions(),
                config.isStackTotems(),
                config.isStackToolsArmor());
        ClientPlayNetworking.send(payload);
    }
}
