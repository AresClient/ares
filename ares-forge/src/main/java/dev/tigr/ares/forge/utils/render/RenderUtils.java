package dev.tigr.ares.forge.utils.render;

import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
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
    public static void prepare3d() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableAlpha();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glPushMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void end3d() {
        GlStateManager.color(1, 1, 1, 1);
        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.enableAlpha();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public static AxisAlignedBB getBoundingBox(BlockPos pos) {
        return MC.world.getBlockState(pos).getBoundingBox(MC.world, pos).offset(pos);
    }

    public static void quadFill(Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

        Mesh.quad(buffer, vertex1, vertex2, vertex3, vertex4);

        tessellator.draw();
    }

    public static void cube(AxisAlignedBB box, Color fill, Color line, EnumFacing... excludeSides) {
        cube(box, fill, line, 4, excludeSides);
    }

    public static void cube(AxisAlignedBB box, Color fill, Color line, float lineThickness, EnumFacing... excludeSides) {
        cube(box, fill, fill, fill, fill, fill, fill, fill, fill, line, line, line, line, line, line, line, line, lineThickness, excludeSides);
    }

    public static void cube(AxisAlignedBB box, Color fill1, Color fill2, Color fill3, Color fill4, Color fill5, Color fill6, Color fill7, Color fill8, Color line1, Color line2, Color line3, Color line4, Color line5, Color line6, Color line7, Color line8, float lineThickness, EnumFacing... excludeSides) {
        cubeFill(box, fill1, fill2, fill3, fill4, fill5, fill6, fill7, fill8, excludeSides);
        cubeLines(box, line1, line2, line3, line4, line5, line6, line7, line8, lineThickness);
    }

    public static void cubeFill(AxisAlignedBB box, Color color, EnumFacing... excludeSides) {
        cubeFill(box, color, color, color, color, color, color, color, color, excludeSides);
    }

    public static void cubeFill(AxisAlignedBB box, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, EnumFacing... excludeSides) {
        Mesh.cube(GL_TRIANGLES, 2, box, color1, color2, color3, color4, color5, color6, color7, color8, excludeSides);
    }

    public static void cubeLines(AxisAlignedBB box, Color color) {
        cubeLines(box, color, 4);
    }

    public static void cubeLines(AxisAlignedBB box, Color color, float lineThickness) {
        cubeLines(box, color, color, color, color, color, color, color, color, lineThickness);
    }

    public static void cubeLines(AxisAlignedBB box, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, float lineThickness) {
        Mesh.cube(GL_LINES, lineThickness, box, color1, color2, color3, color4, color5, color6, color7, color8);
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
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();

        MC.getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
        MC.getRenderItem().renderItemOverlays(MC.fontRenderer, itemStack, x, y);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.disableDepth();
    }

    public static void drawLineSeries(float weight, Vertex... vertices) {
        GlStateManager.disableCull();
        GlStateManager.glLineWidth(weight);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        Mesh.construct(buffer, vertices);

        tessellator.draw();
        GlStateManager.enableCull();
        GlStateManager.glLineWidth(1);
    }

    public static void drawTracer(Vec3d pos, Color color) {
        Vec3d eyeVector = new Vec3d(0.0, 0.0, 70.0)
                .rotatePitch((float) (-Math.toRadians(MC.player.rotationPitch)))
                .rotateYaw((float) (-Math.toRadians(MC.player.rotationYaw)));

        prepare3d();
        GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
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

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
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
}
