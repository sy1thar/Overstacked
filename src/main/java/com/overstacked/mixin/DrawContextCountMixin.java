package com.overstacked.mixin;

import com.overstacked.util.NumberFormatUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;

@Mixin(GuiGraphicsExtractor.class)
public abstract class DrawContextCountMixin {

    @Unique
    private static final HashMap<String, Component> overstacked$textCache = new HashMap<>(256);

    @Redirect(
            method = "itemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;",
                    ordinal = 0
            )
    )
    private String overstacked$formatCount(int count) {
        if (count > 99) {
            return NumberFormatUtil.formatCount(count);
        }
        return String.valueOf(count);
    }

    @Redirect(
            method = "itemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V"
            )
    )
    private void overstacked$scaleCountText(GuiGraphicsExtractor self, Font textRenderer, String text, int x, int y, int color, boolean shadow) {
        int textWidth = textRenderer.width(text);

        int slotX = x + textWidth - 17;
        int slotY = y - 9;

        int slotCenterX = slotX + 8;
        int screenX = slotCenterX - textWidth / 4;
        int screenY = slotY + 12;

        Matrix3x2fStack pose = self.pose();
        pose.pushMatrix();
        pose.translate(screenX, screenY);
        pose.scale(0.5f, 0.5f);
        pose.translate(-screenX, -screenY);

        Component cachedText = overstacked$textCache.get(text);
        if (cachedText == null) {
            cachedText = Component.literal(text);
            overstacked$textCache.put(text, cachedText);
        }
        self.text(textRenderer, cachedText, screenX, screenY, color, shadow);
        pose.popMatrix();
    }
}
