package org.aresclient.ares.mixins;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.aresclient.ares.Ares;
import org.aresclient.ares.renderer.Color;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextRenderer.class)
public abstract class MixinTextRenderer implements org.aresclient.ares.api.TextRenderer {
    @Shadow public abstract int draw(MatrixStack matrices, String text, float x, float y, int color);
    @Shadow public abstract int drawWithShadow(MatrixStack matrices, String text, float x, float y, int color);

    @Override
    public void drawText(String text, float x, float y, Color color) {
        draw(((org.aresclient.ares.impl.MatrixStack) Ares.INSTANCE.minecraft.getRenderer().getRenderStack()).getMatrixStack(), text, x, y, color.getRGB());
    }

    @Override
    public void drawTextWithShadow(String text, float x, float y, Color color) {
        drawWithShadow(((org.aresclient.ares.impl.MatrixStack) Ares.INSTANCE.minecraft.getRenderer().getRenderStack()).getMatrixStack(), text, x, y, color.getRGB());
    }
}
