package dev.tigr.ares.forge.utils.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.forge.mixin.accessor.BufferBuilderAccessor;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Mesh implements Wrapper {
    public static void construct(BufferBuilder buffer, Vertex... vertices) {
        for(Vertex vert: vertices) {
            buffer.pos((vert.x -MC.getRenderManager().viewerPosX), (vert.y -MC.getRenderManager().viewerPosY), (vert.z -MC.getRenderManager().viewerPosZ)).color(vert.r, vert.g, vert.b, vert.a).endVertex();
        }
    }

    public static void triangle(BufferBuilder buffer, Vertex vertex1, Vertex vertex2, Vertex vertex3) {
        if(((BufferBuilderAccessor)buffer).getDrawMode() == GL_TRIANGLES)
            construct(buffer, vertex1, vertex2, vertex3);
        if(((BufferBuilderAccessor)buffer).getDrawMode() == GL_LINES)
            construct(buffer, vertex1, vertex2, vertex2, vertex3, vertex3, vertex1);
    }

    public static void quad(BufferBuilder buffer, Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4) {
        if(((BufferBuilderAccessor)buffer).getDrawMode() == GL_TRIANGLES)
            construct(buffer, vertex1, vertex2, vertex3, vertex1, vertex3, vertex4);
        if(((BufferBuilderAccessor)buffer).getDrawMode() == GL_LINES)
            construct(buffer, vertex1, vertex2, vertex2, vertex3, vertex3, vertex4, vertex4, vertex1);
    }

    public static void cube(int drawMode, float lineWeight, AxisAlignedBB box, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, EnumFacing... excludeSides) {
        cube(drawMode, lineWeight, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color1, color2, color3, color4, color5, color6, color7, color8, excludeSides);
    }

    public static void cube(int drawMode, float lineWeight, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, EnumFacing... excludeSides) {
        Vertex
                vertDNW = new Vertex(minX, minY, minZ, color1),
                vertDNE = new Vertex(maxX, minY, minZ, color2),
                vertDSE = new Vertex(maxX, minY, maxZ, color3),
                vertDSW = new Vertex(minX, minY, maxZ, color4),
                vertUNW = new Vertex(minX, maxY, minZ, color5),
                vertUNE = new Vertex(maxX, maxY, minZ, color6),
                vertUSE = new Vertex(maxX, maxY, maxZ, color7),
                vertUSW = new Vertex(minX, maxY, maxZ, color8);

        cube(drawMode, lineWeight, vertDNW, vertDNE, vertDSE, vertDSW, vertUNW, vertUNE, vertUSE, vertUSW, excludeSides);
    }

    public static void cube(int drawMode, float lineWeight, Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5, Vertex vertex6, Vertex vertex7, Vertex vertex8, EnumFacing... excludeSides) {
        List<EnumFacing> sidesExcluded = Arrays.asList(excludeSides.clone());

        if(drawMode == GL_TRIANGLES) {
            /*  DOWN  */if(!sidesExcluded.contains(EnumFacing.DOWN)) RenderUtils.quadFill(vertex1, vertex2, vertex3, vertex4);
            /*  EAST  */if(!sidesExcluded.contains(EnumFacing.EAST)) RenderUtils.quadFill(vertex3, vertex2, vertex6, vertex7);
            /*  SOUTH */if(!sidesExcluded.contains(EnumFacing.SOUTH)) RenderUtils.quadFill(vertex7, vertex8, vertex4, vertex3);
            /*  WEST  */if(!sidesExcluded.contains(EnumFacing.WEST)) RenderUtils.quadFill(vertex8, vertex5, vertex1, vertex4);
            /*  NORTH */if(!sidesExcluded.contains(EnumFacing.NORTH)) RenderUtils.quadFill(vertex2, vertex1, vertex5, vertex6);
            /*  UP    */if(!sidesExcluded.contains(EnumFacing.UP)) RenderUtils.quadFill(vertex6, vertex5, vertex8, vertex7);
        }
        if(drawMode == GL_LINES) {
            GlStateManager.disableCull();
            GlStateManager.glLineWidth(lineWeight);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            construct(buffer,
                    vertex1, vertex2,
                    vertex2, vertex3,
                    vertex3, vertex4,
                    vertex4, vertex1,
                    vertex1, vertex5,
                    vertex2, vertex6,
                    vertex3, vertex7,
                    vertex4, vertex8,
                    vertex5, vertex6,
                    vertex6, vertex7,
                    vertex7, vertex8,
                    vertex8, vertex5
            );

            tessellator.draw();

            GlStateManager.enableCull();
            GlStateManager.glLineWidth(1);
        }
    }
}
