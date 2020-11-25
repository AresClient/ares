package dev.tigr.ares.forge.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.impl.modules.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "PlayerPreview", description = "Shows a preview of what your player looks like on the hud", category = Category.HUD)
public class PlayerPreview extends HudElement {
    public PlayerPreview() {
        super(300, 100, 25, 25);
        background.setVisibility(() -> false);
    }

    @Override
    public void drawBackground() {  }

    public void draw() {
        GlStateManager.pushMatrix();
        GlStateManager.color(1, 1, 1);
        GlStateManager.rotate(0, 0, 5, 0);

        drawPlayer(getX() + getWidth(), getY() + getHeight(), MC.player);

        GlStateManager.popMatrix();
    }

    private void drawPlayer(int p_drawEntityOnScreen_0_, int p_drawEntityOnScreen_1_, EntityLivingBase p_drawEntityOnScreen_5_) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) p_drawEntityOnScreen_0_, (float) p_drawEntityOnScreen_1_, 50.0F);
        GlStateManager.scale((float) (-50), (float) 50, (float) 50);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float) Math.atan(((float) 0 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager lvt_11_1_ = Minecraft.getMinecraft().getRenderManager();
        lvt_11_1_.setPlayerViewY(180.0F);
        lvt_11_1_.setRenderShadow(false);
        lvt_11_1_.renderEntity(p_drawEntityOnScreen_5_, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        lvt_11_1_.setRenderShadow(true);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.popMatrix();
    }
}
