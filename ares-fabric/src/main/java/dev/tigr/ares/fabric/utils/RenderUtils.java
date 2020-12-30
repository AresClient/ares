package dev.tigr.ares.fabric.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/**
 * @author Tigermouthbear 8/11/20
 */
public class RenderUtils extends DrawableHelper implements Wrapper {
    /**
     * Returns the current color of the cycling rainbow
     *
     * @return color
     */
    public static Color rainbow() {
        float hue = (System.currentTimeMillis() % (320 * 32)) / (320f * 32);
        return new Color(Color.HSBtoRGB(hue, 1, 1));
    }

    public static void glBegin() {
        GL11.glPushMatrix();
        RenderSystem.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.lineWidth(2F);
        RenderSystem.disableTexture();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableDepthTest();

        Camera camera = BlockEntityRenderDispatcher.INSTANCE.camera;
        Vec3d position = camera.getPos();
        GL11.glRotated(MathHelper.wrapDegrees(camera.getPitch()), 1, 0, 0);
        GL11.glRotated(MathHelper.wrapDegrees(camera.getYaw() + 180.0), 0, 1, 0);
        GL11.glTranslated(-position.x, -position.y, -position.z);
    }

    public static void glEnd() {
        RenderSystem.enableDepthTest();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        GL11.glPopMatrix();
    }

    public static void renderItemStack(ItemStack stack, int x, int y) {
        MC.getItemRenderer().renderInGuiWithOverrides(stack, x, y);
        MC.getItemRenderer().renderGuiItemOverlay(MC.textRenderer, stack, x, y);
        RenderSystem.enableBlend();
    }

    public static Box getBoundingBox(BlockPos pos) {
        try {
            assert MC.world != null;
            return MC.world.getBlockState(pos).getOutlineShape(MC.world, pos).getBoundingBox().offset(pos);
        } catch(Exception e) {
            return null;
        }
    }

    public static void renderSelectionBoundingBox(Box box, Color color) {
        renderSelectionBoundingBox(box, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static void renderSelectionBoundingBox(Box box, float red, float green, float blue, float alpha) {
        renderBoundingBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
    }

    public static void renderBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha)  {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, VertexFormats.POSITION_COLOR);

        buffer.vertex(minX, minY, minZ).color(red, green, blue, 0.0F).next();
        buffer.vertex(minX, minY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(minX, minY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(minX, maxY, maxZ).color(red, green, blue, 0.0F).next();
        buffer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(maxX, maxY, maxZ).color(red, green, blue, 0.0F).next();
        buffer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(maxX, maxY, minZ).color(red, green, blue, 0.0F).next();
        buffer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(maxX, minY, minZ).color(red, green, blue, 0.0F).next();

        tessellator.draw();
    }

    public static void renderFilledBox(Box box, Color color) {
        renderFilledBox(box, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static void renderFilledBox(Box box, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(5, VertexFormats.POSITION_COLOR);
        WorldRenderer.drawBox(bufferBuilder,
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ, red, green, blue, alpha / 2f);
        tessellator.draw();
    }

    public static void drawTracer(Entity entity, Color color) {
        RenderSystem.disableDepthTest();
        Vec3d pos = entity.getPos();

        Vec3d eyeVector = new Vec3d(0.0, 0.0, 75)
                .rotateX((float) (-Math.toRadians(MC.cameraEntity.pitch)))
                .rotateY((float) (-Math.toRadians(MC.cameraEntity.yaw)))
                .add(MC.cameraEntity.getPos()
                        .add(0, MC.cameraEntity.getEyeHeight(MC.cameraEntity.getPose()), 0));

        RENDERER.drawLine(pos.x, pos.y, pos.z, eyeVector.x, eyeVector.y, eyeVector.z, 2, color);
        RENDERER.drawLine(pos.x, pos.y, pos.z, pos.x, pos.y + entity.getHeight(), pos.z, 2, color);
    }

    public static void drawTracer(Vec3d pos, Color color) {
        RenderSystem.disableDepthTest();
        Vec3d eyeVector = new Vec3d(0.0, 0.0, 75)
                .rotateX((float) (-Math.toRadians(MC.player.pitch)))
                .rotateY((float) (-Math.toRadians(MC.player.yaw)))
                .add(MC.cameraEntity.getPos()
                .add(0, MC.cameraEntity.getEyeHeight(MC.cameraEntity.getPose()), 0));

        RENDERER.drawLine(pos.x, pos.y, pos.z, eyeVector.x, eyeVector.y, eyeVector.z, 2, color);
    }

    public static Vec3d getRenderPos(Entity entity) {
        Vec3d cameraPos = MC.gameRenderer.getCamera().getPos();
        double x = (entity.prevX + (entity.getX() - entity.prevX) * MC.getTickDelta()) - cameraPos.x;
        double y = (entity.prevY + (entity.getY() - entity.prevY) * MC.getTickDelta()) - cameraPos.y;
        double z = (entity.prevZ + (entity.getZ() - entity.prevZ) * MC.getTickDelta()) - cameraPos.z;

        return new Vec3d(x, y, z);
    }
}
