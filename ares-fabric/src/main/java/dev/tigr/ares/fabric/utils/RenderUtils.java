package dev.tigr.ares.fabric.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.impl.render.CustomRenderStack;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;

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

    public static void prepare3d() {
        RENDER_STACK.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);

        RENDER_STACK.rotate(MathHelper.wrapDegrees(MC.gameRenderer.getCamera().getPitch()), 1, 0, 0);
        RENDER_STACK.rotate((float) MathHelper.wrapDegrees(MC.gameRenderer.getCamera().getYaw() + 180.0), 0, 1, 0);
    }

    public static void end3d() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RENDER_STACK.pop();
    }

    // Entity Box
    public static void renderEntityBox(Entity entity, Color fillColor, Color outlineColor) {
        renderEntityBox(entity, fillColor, outlineColor, 2f, 0);
    }
    public static void renderEntityBox(Entity entity, Color fillColor, Color outlineColor, float outlineThickness, float expansion) {
        prepare3d();
        renderEntityBoxNoPrepare(entity, fillColor, outlineColor, outlineThickness, expansion);
        end3d();
    }
    public static void renderEntityBoxNoPrepare(Entity entity, Color fillColor, Color outlineColor) {
        renderEntityBoxNoPrepare(entity, fillColor, outlineColor, 2f, 0);
    }
    public static void renderEntityBoxNoPrepare(Entity entity, Color fillColor, Color outlineColor, float outlineThickness, float expansion) {
        Box renderBox = entity.getBoundingBox()
                .expand(expansion)
                .offset(
                        -MC.gameRenderer.getCamera().getPos().x,
                        -MC.gameRenderer.getCamera().getPos().y,
                        -MC.gameRenderer.getCamera().getPos().z
                );
        renderBlockNoPrepare(renderBox, fillColor, outlineColor, outlineThickness);
    }

    // Block by pos
    public static void renderBlock(BlockPos pos, Color fillColor, Color outlineColor) {
        renderBlock(pos, fillColor, outlineColor, 2f, 0);
    }
    public static void renderBlock(BlockPos pos, Color fillColor, Color outlineColor, float outlineThickness, float expansion) {
        if(!MC.world.getBlockState(pos).isAir()) {
            prepare3d();
            renderBlockNoPrepare(pos, fillColor, outlineColor, outlineThickness, expansion);
            end3d();
        }
    }
    public static void renderBlockNoPrepare(BlockPos pos, Color fillColor, Color outlineColor) {
        renderBlockNoPrepare(pos, fillColor, outlineColor, 2f, 0);
    }
    public static void renderBlockNoPrepare(BlockPos pos, Color fillColor, Color outlineColor, float outlineThickness, float expansion) {
        Box renderBox = getBoundingBox(pos)
                .expand(expansion)
                .offset(
                        -MC.gameRenderer.getCamera().getPos().x,
                        -MC.gameRenderer.getCamera().getPos().y,
                        -MC.gameRenderer.getCamera().getPos().z
                );
        renderBlockNoPrepare(renderBox, fillColor, outlineColor, outlineThickness);
    }

    // Block by box
    public static void renderBlock(Box renderBox, Color fillColor, Color outlineColor) {
        renderBlock(renderBox, fillColor, outlineColor, 2f);
    }
    public static void renderBlock(Box renderBox, Color fillColor, Color outlineColor, float outlineThickness) {
        prepare3d();
        renderBlockNoPrepare(renderBox, fillColor, outlineColor, outlineThickness);
        end3d();
    }
    public static void renderBlockNoPrepare(Box renderBox, Color fillColor, Color outlineColor) {
        renderBlockNoPrepare(renderBox, fillColor, outlineColor, 2f);
    }
    public static void renderBlockNoPrepare(Box renderBox, Color fillColor, Color outlineColor, float outlineThickness) {
        if(fillColor.getAlpha() != 0) renderFilledBox(renderBox, fillColor);
        if(outlineColor.getAlpha() != 0) renderSelectionBoundingBox(renderBox, outlineColor, outlineThickness);
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

    public static void renderSelectionBoundingBox(Box box, Color color, float lineThickness) {
        renderSelectionBoundingBox(box, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), lineThickness);
    }

    public static void renderSelectionBoundingBox(Box box, float red, float green, float blue, float alpha, float lineThickness) {
        renderBoundingBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha, lineThickness);
    }

    public static void renderBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha, float lineThickness) {
        renderBoundingBox((float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ, red, green, blue, alpha, lineThickness);
    }

    public static void renderBoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha, float lineThickness) {
        RenderSystem.lineWidth(lineThickness);

        Matrix4f model = getModel();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);

        buffer.vertex(model, minX, minY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, minX, minY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, maxX, minY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, maxX, minY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, minX, minY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, minX, maxY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, minX, maxY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, minX, minY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, maxX, minY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, maxX, maxY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, minX, maxY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, maxX, maxY, maxZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, maxX, maxY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, maxX, minY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, maxX, maxY, minZ).color(red, green, blue, alpha).next();
        buffer.vertex(model, minX, maxY, minZ).color(red, green, blue, alpha).next();

        tessellator.draw();
    }

    public static void renderFilledBox(Box box, Color color) {
        renderFilledBox(box, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static void renderFilledBox(Box box, float red, float green, float blue, float alpha) {
        renderFilledBox(
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ,
                red, green, blue, alpha /2f);
    }

    public static void renderFilledBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        WorldRenderer.drawBox(getMatrix(), bufferBuilder,
                minX, minY, minZ,
                maxX, maxY, maxZ, red, green, blue, alpha /2f);
        tessellator.draw();
    }

    public static void drawLine(Vec3d point1, Vec3d point2, int weight, Color color) {
        drawLine(point1.x, point1.y, point1.z, point2.x, point2.y, point2.z, weight, color);
    }
    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, int weight, Color color) {
        RENDERER.drawLine(
                x1 -MC.gameRenderer.getCamera().getPos().x,
                y1 -MC.gameRenderer.getCamera().getPos().y,
                z1 -MC.gameRenderer.getCamera().getPos().z,
                x2 -MC.gameRenderer.getCamera().getPos().x,
                y2 -MC.gameRenderer.getCamera().getPos().y,
                z2 -MC.gameRenderer.getCamera().getPos().z,
                weight,
                color
        );
    }

    public static void drawTracer(Entity entity, Color color) {
        RenderSystem.disableDepthTest();
        Vec3d pos = entity.getPos();

        Vec3d eyeVector = new Vec3d(0.0, 0.0, 75)
                .rotateX((float) (-Math.toRadians(MC.cameraEntity.getPitch())))
                .rotateY((float) (-Math.toRadians(MC.cameraEntity.getYaw())))
                .add(MC.cameraEntity.getPos()
                        .add(0, MC.cameraEntity.getEyeHeight(MC.cameraEntity.getPose()), 0));

        drawLine(pos, eyeVector, 2, color);
        drawLine(pos.x, pos.y, pos.z, pos.x, pos.y + entity.getHeight(), pos.z, 2, color);
    }

    public static void drawTracer(Vec3d pos, Color color) {
        RenderSystem.disableDepthTest();
        Vec3d eyeVector = new Vec3d(0.0, 0.0, 75)
                .rotateX((float) (-Math.toRadians(MC.player.getPitch())))
                .rotateY((float) (-Math.toRadians(MC.player.getYaw())))
                .add(MC.cameraEntity.getPos()
                .add(0, MC.cameraEntity.getEyeHeight(MC.cameraEntity.getPose()), 0));

        drawLine(pos, eyeVector, 2, color);
    }

    public static Vec3d getRenderPos(Entity entity) {
        Vec3d cameraPos = MC.gameRenderer.getCamera().getPos();
        double x = (entity.prevX + (entity.getX() - entity.prevX) * MC.getTickDelta()) - cameraPos.x;
        double y = (entity.prevY + (entity.getY() - entity.prevY) * MC.getTickDelta()) - cameraPos.y;
        double z = (entity.prevZ + (entity.getZ() - entity.prevZ) * MC.getTickDelta()) - cameraPos.z;

        return new Vec3d(x, y, z);
    }

    private static MatrixStack getMatrix() {
        return ((CustomRenderStack) RENDER_STACK).getMatrixStack();
    }

    private static Matrix4f getModel() {
        return ((CustomRenderStack) RENDER_STACK).getMatrixStack().peek().getModel();
    }
}
