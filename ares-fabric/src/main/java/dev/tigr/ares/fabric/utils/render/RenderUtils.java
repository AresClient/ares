package dev.tigr.ares.fabric.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.math.doubles.V3D;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.Vertex;
import dev.tigr.ares.fabric.impl.render.CustomRenderStack;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;

import static net.minecraft.client.render.VertexFormat.DrawMode.*;

/**
 * @author Tigermouthbear 8/11/20
 */
public class RenderUtils extends DrawableHelper implements Wrapper {
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

    public static Box getBoundingBox(BlockPos pos) {
        try {
            assert MC.world != null;
            return MC.world.getBlockState(pos).getOutlineShape(MC.world, pos).getBoundingBox().offset(pos);
        } catch(Exception e) {
            return null;
        }
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
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(QUADS, VertexFormats.POSITION_COLOR);

        Mesh.cube(buffer, box, color1, color2, color3, color4, color5, color6, color7, color8, excludeSides);

        tessellator.draw();
    }

    public static void cubeLines(Box box, Color color) {
        cubeLines(box, color, 4);
    }

    public static void cubeLines(Box box, Color color, float lineThickness) {
        cubeLines(box, color, color, color, color, color, color, color, color, lineThickness);
    }

    public static void cubeLines(Box box, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, float lineThickness) {
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
        RenderSystem.lineWidth(lineThickness);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(LINES, VertexFormats.LINES);

        Mesh.cube(buffer, box, color1, color2, color3, color4, color5, color6, color7, color8);

        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.lineWidth(1);
    }

    public static void renderItemStack(ItemStack stack, int x, int y) {
        MC.getItemRenderer().renderInGuiWithOverrides(stack, x, y);
        MC.getItemRenderer().renderGuiItemOverlay(MC.textRenderer, stack, x, y);
        RenderSystem.enableBlend();
    }

    public static void drawLine(Vertex vertex1, Vertex vertex2, float weight) {
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
        RenderSystem.lineWidth(weight);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(LINES, VertexFormats.LINES);

        Mesh.construct(buffer, vertex1, vertex2);

        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.lineWidth(1);
    }

    public static void drawLineSeries(float weight, Vertex... vertices) {
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
        RenderSystem.lineWidth(weight);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(LINE_STRIP, VertexFormats.LINES);

        Vertex prevVert = null;
        for(Vertex vertex: vertices) {
            if(prevVert != null) {
                Mesh.construct(buffer, prevVert, vertex);
            }
            prevVert = vertex;
        }

        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.lineWidth(1);
    }

    public static void drawTracer(Entity entity, Color color) {
        RenderSystem.disableDepthTest();
        Vec3d pos = new Vec3d(entity.getX(), entity.getY() + entity.getHeight()/2, entity.getZ());

        Vec3d eyePos = MC.cameraEntity.getPos().add(0, MC.cameraEntity.getEyeHeight(MC.cameraEntity.getPose()), 0);
        V3D eyeVector = new V3D(0.0, 0.0, 75)
                .rotateX((float) (-Math.toRadians(MC.cameraEntity.getPitch())))
                .rotateY((float) (-Math.toRadians(MC.cameraEntity.getYaw())))
                .add(eyePos.getX(), eyePos.getY(), eyePos.getZ());

        drawLine(new Vertex(new V3D(pos.getX(), pos.getY(), pos.getZ()), color), new Vertex(eyeVector, color), 2);
        drawLine(new Vertex(pos.x, pos.y - entity.getHeight()/2, pos.z, color), new Vertex(pos.x, pos.y + entity.getHeight()/2, pos.z, color), 2);
    }

    public static void drawTracer(Vec3d pos, Color color) {
        RenderSystem.disableDepthTest();

        Vec3d eyePos = MC.cameraEntity.getPos().add(0, MC.cameraEntity.getEyeHeight(MC.cameraEntity.getPose()), 0);
        V3D eyeVector = new V3D(0.0, 0.0, 75)
                .rotateX((float) (-Math.toRadians(MC.player.getPitch())))
                .rotateY((float) (-Math.toRadians(MC.player.getYaw())))
                .add(eyePos.getX(), eyePos.getY(), eyePos.getZ());

        drawLine(new Vertex(new V3D(pos.getX(), pos.getY(), pos.getZ()), color), new Vertex(eyeVector, color), 2);
    }

    private static MatrixStack getMatrix() {
        return ((CustomRenderStack) RENDER_STACK).getMatrixStack();
    }

    public static Matrix4f getModel() {
        return ((CustomRenderStack) RENDER_STACK).getMatrixStack().peek().getPositionMatrix();
    }

    public static Matrix3f getNormal() {
        return ((CustomRenderStack) RENDER_STACK).getMatrixStack().peek().getNormalMatrix();
    }

    public static Vec3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        Vec3f normal = new Vec3f(x2 - x1, y2 - y1, z2 - z1);
        normal.normalize();
        return normal;
    }
}
