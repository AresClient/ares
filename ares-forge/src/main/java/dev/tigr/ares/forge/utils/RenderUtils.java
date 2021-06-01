package dev.tigr.ares.forge.utils;

import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import static dev.tigr.ares.Wrapper.MC;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Tigermouthbear
 */
public class RenderUtils {
    /**
     * Returns the current color of the cycling rainbow
     *
     * @return color
     */
    public static Color rainbow() {
        float hue = (System.currentTimeMillis() % (320 * 32)) / (320f * 32);
        return new Color(Color.HSBtoRGB(hue, 1, 1));
    }

    /**
     * Renders an item onto the GUI
     *
     * @param itemStack item stack to render
     * @param x         x position to render at
     * @param y         y position to render at
     */
    public static void renderItem(ItemStack itemStack, int x, int y) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();

        MC.getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
        MC.getRenderItem().renderItemOverlays(MC.fontRenderer, itemStack, x, y);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.disableDepth();
    }

    public static void drawTracer(Vec3d pos, Color color) {
        Vec3d eyeVector = new Vec3d(0.0, 0.0, 70.0)
                .rotatePitch((float) (-Math.toRadians(MC.player.rotationPitch)))
                .rotateYaw((float) (-Math.toRadians(MC.player.rotationYaw)));

        prepare3d();
        GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(eyeVector.x + MC.getRenderManager().viewerPosX, (double) MC.player.getEyeHeight() + eyeVector.y + MC.getRenderManager().viewerPosY, eyeVector.z + MC.getRenderManager().viewerPosZ);
        GL11.glVertex3d(pos.x, pos.y, pos.z);
        GL11.glEnd();
        end3d();
    }

    public static void drawTracer(Entity entity, Color color) {
        Vec3d pos = getRenderPos(entity);

        Vec3d eyeVector = new Vec3d(0.0, 0.0, 70.0)
                .rotatePitch((float) (-Math.toRadians(MC.player.rotationPitch)))
                .rotateYaw((float) (-Math.toRadians(MC.player.rotationYaw)));

        GL11.glBlendFunc(GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(eyeVector.x, (double) MC.player.getEyeHeight() + eyeVector.y, eyeVector.z);
        GL11.glVertex3d(pos.x, pos.y, pos.z);
        GL11.glVertex3d(pos.x, pos.y, pos.z);
        GL11.glVertex3d(pos.x, pos.y + entity.height, pos.z);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.resetColor();
    }

    public static Vec3d getRenderPos(Entity entity) {
        double x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * MC.getRenderPartialTicks()
                - (double) ReflectionHelper.getPrivateValue(RenderManager.class, MC.getRenderManager(), "renderPosX", "field_78725_b"));
        double y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * MC.getRenderPartialTicks()
                - (double) ReflectionHelper.getPrivateValue(RenderManager.class, MC.getRenderManager(), "renderPosY", "field_78726_c"));
        double z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * MC.getRenderPartialTicks()
                - (double) ReflectionHelper.getPrivateValue(RenderManager.class, MC.getRenderManager(), "renderPosZ", "field_78723_d"));

        return new Vec3d(x, y, z);
    }

    public static void prepare3d() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(2);
        GlStateManager.disableAlpha();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        double renderPosX = MC.getRenderManager().viewerPosX;
        double renderPosY = MC.getRenderManager().viewerPosY;
        double renderPosZ = MC.getRenderManager().viewerPosZ;

        glPushMatrix();
        glTranslated(-renderPosX, -renderPosY, -renderPosZ);
        glColor4f(1, 1, 1, 1);
    }

    public static void end3d() {
        glColor4f(1, 1, 1, 1);
        glPopMatrix();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        GlStateManager.enableAlpha();
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
    }

    public static AxisAlignedBB getBoundingBox(BlockPos pos) {
        return MC.world.getBlockState(pos).getBoundingBox(MC.world, pos).offset(pos);
    }
}
