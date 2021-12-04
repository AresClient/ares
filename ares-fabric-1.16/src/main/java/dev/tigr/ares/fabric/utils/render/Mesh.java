package dev.tigr.ares.fabric.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.mixin.accessors.BufferBuilderAccessor;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class Mesh implements Wrapper {
    public static void construct(BufferBuilder buffer, Vertex... vertices) {
        for(Vertex vert: vertices) {
            buffer.vertex((vert.x -MC.gameRenderer.getCamera().getPos().x), (vert.y -MC.gameRenderer.getCamera().getPos().y), (vert.z -MC.gameRenderer.getCamera().getPos().z)).color(vert.r, vert.g, vert.b, vert.a).next();
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

    public static void cube(int drawMode, float lineWeight, Box box, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, Direction... excludeSides) {
        cube(drawMode, lineWeight, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color1, color2, color3, color4, color5, color6, color7, color8, excludeSides);
    }

    public static void cube(int drawMode, float lineWeight, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, Direction... excludeSides) {
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

    public static void cube(int drawMode, float lineWeight, Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5, Vertex vertex6, Vertex vertex7, Vertex vertex8, Direction... excludeSides) {
        List<Direction> sidesExcluded = Arrays.asList(excludeSides.clone());

        if(drawMode == GL_TRIANGLES) {
            /*  DOWN  */if(!sidesExcluded.contains(Direction.DOWN)) RenderUtils.quadFill(vertex1, vertex2, vertex3, vertex4);
            /*  EAST  */if(!sidesExcluded.contains(Direction.EAST)) RenderUtils.quadFill(vertex3, vertex2, vertex6, vertex7);
            /*  SOUTH */if(!sidesExcluded.contains(Direction.SOUTH)) RenderUtils.quadFill(vertex7, vertex8, vertex4, vertex3);
            /*  WEST  */if(!sidesExcluded.contains(Direction.WEST)) RenderUtils.quadFill(vertex8, vertex5, vertex1, vertex4);
            /*  NORTH */if(!sidesExcluded.contains(Direction.NORTH)) RenderUtils.quadFill(vertex2, vertex1, vertex5, vertex6);
            /*  UP    */if(!sidesExcluded.contains(Direction.UP)) RenderUtils.quadFill(vertex6, vertex5, vertex8, vertex7);
        }
        if(drawMode == GL_LINES) {
            RenderSystem.disableCull();
            RenderSystem.lineWidth(lineWeight);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL_LINES, VertexFormats.POSITION_COLOR);

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
            RenderSystem.enableCull();
            RenderSystem.lineWidth(1);
        }
    }
}
