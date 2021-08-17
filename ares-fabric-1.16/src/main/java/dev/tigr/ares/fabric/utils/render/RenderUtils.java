package dev.tigr.ares.fabric.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Tigermouthbear 8/11/20
 */
public class RenderUtils extends DrawableHelper implements Wrapper {
    public static void prepare3d() {
        GL11.glPushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        GL11.glDisable(GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        GL11.glRotated(MathHelper.wrapDegrees(MC.gameRenderer.getCamera().getPitch()), 1, 0, 0);
        GL11.glRotated(MathHelper.wrapDegrees(MC.gameRenderer.getCamera().getYaw() + 180.0), 0, 1, 0);
    }

    public static void end3d() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL_ALPHA_TEST);
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        GL11.glPopMatrix();
    }

    public static Box getBoundingBox(BlockPos pos) {
        try {
            assert MC.world != null;
            return MC.world.getBlockState(pos).getOutlineShape(MC.world, pos).getBoundingBox().offset(pos);
        } catch(Exception e) {
            return null;
        }
    }

    public static void quadFill(Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL_TRIANGLES, VertexFormats.POSITION_COLOR);

        Mesh.quad(buffer, vertex1, vertex2, vertex3, vertex4);

        tessellator.draw();
    }

    public static void cube(Box box, Color fill, Color line, Direction... excludeSides) {
        cube(box, fill, line, 4, excludeSides);
    }

    public static void cube(Box box, Color fill, Color line, float lineThickness, Direction... excludeSides) {
        cube(box, fill, fill, fill, fill, fill, fill, fill, fill, line, line, line, line, line, line, line, line, lineThickness, excludeSides);
    }

    public static void cube(Box box, Color fill1, Color fill2, Color fill3, Color fill4, Color fill5, Color fill6, Color fill7, Color fill8, Color line1, Color line2, Color line3, Color line4, Color line5, Color line6, Color line7, Color line8, float lineThickness, Direction... excludeSides) {
        cubeFill(box, fill1, fill2, fill3, fill4, fill5, fill6, fill7, fill8, excludeSides);
        cubeLines(box, line1, line2, line3, line4, line5, line6, line7, line8, lineThickness);
    }

    public static void cubeFill(Box box, Color color, Direction... excludeSides) {
        cubeFill(box, color, color, color, color, color, color, color, color, excludeSides);
    }

    public static void cubeFill(Box box, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, Direction... excludeSides) {
        Mesh.cube(GL_TRIANGLES, 2, box, color1, color2, color3, color4, color5, color6, color7, color8, excludeSides);
    }

    public static void cubeLines(Box box, Color color) {
        cubeLines(box, color, 4);
    }

    public static void cubeLines(Box box, Color color, float lineThickness) {
        cubeLines(box, color, color, color, color, color, color, color, color, lineThickness);
    }

    public static void cubeLines(Box box, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, float lineThickness) {
        Mesh.cube(GL_LINES, lineThickness, box, color1, color2, color3, color4, color5, color6, color7, color8);
    }

    public static void renderItemStack(ItemStack stack, int x, int y) {
        MC.getItemRenderer().renderInGuiWithOverrides(stack, x, y);
        MC.getItemRenderer().renderGuiItemOverlay(MC.textRenderer, stack, x, y);
        RenderSystem.enableBlend();
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

    public static void drawLineSeries(float weight, Vertex... vertices) {
        RenderSystem.disableCull();
        RenderSystem.lineWidth(weight);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL_LINE_STRIP, VertexFormats.POSITION_COLOR);

        Mesh.construct(buffer, vertices);

        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.lineWidth(1);
    }

    public static void drawTracer(Entity entity, Color color) {
        RenderSystem.disableDepthTest();
        Vec3d pos = entity.getPos();

        Vec3d eyeVector = new Vec3d(0.0, 0.0, 75)
                .rotateX((float) (-Math.toRadians(MC.cameraEntity.pitch)))
                .rotateY((float) (-Math.toRadians(MC.cameraEntity.yaw)))
                .add(MC.cameraEntity.getPos()
                        .add(0, MC.cameraEntity.getEyeHeight(MC.cameraEntity.getPose()), 0));

        drawLine(pos, eyeVector, 2, color);
        drawLine(pos.x, pos.y, pos.z, pos.x, pos.y + entity.getHeight(), pos.z, 2, color);
    }

    public static void drawTracer(Vec3d pos, Color color) {
        RenderSystem.disableDepthTest();
        Vec3d eyeVector = new Vec3d(0.0, 0.0, 75)
                .rotateX((float) (-Math.toRadians(MC.player.pitch)))
                .rotateY((float) (-Math.toRadians(MC.player.yaw)))
                .add(MC.cameraEntity.getPos()
                .add(0, MC.cameraEntity.getEyeHeight(MC.cameraEntity.getPose()), 0));

        drawLine(pos, eyeVector, 2, color);
    }
}
