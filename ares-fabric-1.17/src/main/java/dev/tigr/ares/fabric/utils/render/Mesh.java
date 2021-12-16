package dev.tigr.ares.fabric.utils.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.Vertex;
import dev.tigr.ares.fabric.mixin.accessors.BufferBuilderAccessor;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.client.render.VertexFormat.DrawMode.LINES;
import static net.minecraft.client.render.VertexFormat.DrawMode.LINE_STRIP;

public class Mesh implements Wrapper {
    private static void vertex(BufferBuilder buffer, Vertex vert, Vec3f normalVec) {
        Matrix4f model = RenderUtils.getModel();
        VertexFormat.DrawMode drawMode = ((BufferBuilderAccessor) buffer).getDrawMode();

        if((drawMode == LINES || drawMode == LINE_STRIP) && normalVec != null) {
            Matrix3f normal = RenderUtils.getNormal();
            buffer.vertex(model, (float)(vert.x -MC.gameRenderer.getCamera().getPos().x), (float)(vert.y -MC.gameRenderer.getCamera().getPos().y), (float)(vert.z -MC.gameRenderer.getCamera().getPos().z)).color(vert.r, vert.g, vert.b, vert.a).normal(normal, normalVec.getX(), normalVec.getY(), normalVec.getZ()).next();
        }
        else
            buffer.vertex(model, (float)(vert.x -MC.gameRenderer.getCamera().getPos().x), (float)(vert.y -MC.gameRenderer.getCamera().getPos().y), (float)(vert.z -MC.gameRenderer.getCamera().getPos().z)).color(vert.r, vert.g, vert.b, vert.a).next();
    }

    public static void construct(BufferBuilder buffer, Vertex... vertices) {
        VertexFormat.DrawMode drawMode = ((BufferBuilderAccessor) buffer).getDrawMode();
        if(drawMode == LINES || drawMode == LINE_STRIP) {
            List<Line> lines = new ArrayList<>();
            Vertex lastVertex = null;
            Vertex firstVertex = null;

            for(Vertex vertex: vertices) {
                if(firstVertex == null) firstVertex = vertex;
                if(lastVertex != null) {
                    lines.add(new Line(lastVertex, vertex));
                    lastVertex = null;
                }
                else lastVertex = vertex;
            }

            if(drawMode == LINES) {
                for(Line line: lines) {
                    vertex(buffer, line.vertex1, line.normalVec);
                    vertex(buffer, line.vertex2, line.normalVec);
                }
            } else {
                vertex(buffer, firstVertex, lines.get(0).normalVec);

                for(Line line: lines) {
                    vertex(buffer, line.vertex2, line.normalVec);
                }
            }
        }
        else for(Vertex vert: vertices) vertex(buffer, vert, null);
    }

    private static class Line {
        Vertex
                vertex1,
                vertex2;
        public Vec3f normalVec;

        Line(Vertex vertex1, Vertex vertex2) {
            this.vertex1 = vertex1;
            this.vertex2 = vertex2;

            normalVec = RenderUtils.getNormal((float)vertex1.x, (float)vertex1.y, (float)vertex1.z, (float)vertex2.x, (float)vertex2.y, (float)vertex2.z);
        }
    }

    public static void triangle(BufferBuilder buffer, Vertex vertex1, Vertex vertex2, Vertex vertex3) {
        switch(((BufferBuilderAccessor)buffer).getDrawMode()) {
            case TRIANGLES -> construct(buffer, vertex1, vertex2, vertex3);
            case LINES -> construct(buffer, vertex1, vertex2, vertex2, vertex3, vertex3, vertex1);
        }
    }

    public static void quad(BufferBuilder buffer, Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4) {
        switch(((BufferBuilderAccessor)buffer).getDrawMode()) {
            case QUADS -> construct(buffer, vertex1, vertex2, vertex3, vertex4);
            case TRIANGLES -> construct(buffer, vertex1, vertex2, vertex3, vertex1, vertex3, vertex4);
            case LINES -> construct(buffer, vertex1, vertex2, vertex2, vertex3, vertex3, vertex4, vertex4, vertex1);
        }
    }

    public static void cube(BufferBuilder buffer, Box box, Color colorA1, Color colorA2, Color colorA3, Color colorA4, Color colorB1, Color colorB2, Color colorB3, Color colorB4, Direction... excludeSides) {
        cube(buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, colorA1, colorA2, colorA3, colorA4, colorB1, colorB2, colorB3, colorB4, excludeSides);
    }

    public static void cube(BufferBuilder buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color colorA1, Color colorA2, Color colorA3, Color colorA4, Color colorB1, Color colorB2, Color colorB3, Color colorB4, Direction... excludeSides) {
        Vertex
                vertDNW = new Vertex(minX, minY, minZ, colorA1),
                vertDNE = new Vertex(maxX, minY, minZ, colorA2),
                vertDSE = new Vertex(maxX, minY, maxZ, colorA3),
                vertDSW = new Vertex(minX, minY, maxZ, colorA4),
                vertUNW = new Vertex(minX, maxY, minZ, colorB1),
                vertUNE = new Vertex(maxX, maxY, minZ, colorB2),
                vertUSE = new Vertex(maxX, maxY, maxZ, colorB3),
                vertUSW = new Vertex(minX, maxY, maxZ, colorB4);

        cube(buffer, vertDNW, vertDNE, vertDSE, vertDSW, vertUNW, vertUNE, vertUSE, vertUSW, excludeSides);
    }

    public static void cube(BufferBuilder buffer, Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5, Vertex vertex6, Vertex vertex7, Vertex vertex8, Direction... excludeSides) {
        List<Direction> sidesExcluded = Arrays.asList(excludeSides.clone());

        switch(((BufferBuilderAccessor)buffer).getDrawMode()) {
            case QUADS, TRIANGLES -> {
                /*  DOWN  */if(!sidesExcluded.contains(Direction.DOWN)) quad(buffer, vertex1, vertex2, vertex3, vertex4);
                /*  EAST  */if(!sidesExcluded.contains(Direction.EAST)) quad(buffer, vertex3, vertex2, vertex6, vertex7);
                /*  SOUTH */if(!sidesExcluded.contains(Direction.SOUTH)) quad(buffer, vertex7, vertex8, vertex4, vertex3);
                /*  WEST  */if(!sidesExcluded.contains(Direction.WEST)) quad(buffer, vertex8, vertex5, vertex1, vertex4);
                /*  NORTH */if(!sidesExcluded.contains(Direction.NORTH)) quad(buffer, vertex2, vertex1, vertex5, vertex6);
                /*  UP    */if(!sidesExcluded.contains(Direction.UP)) quad(buffer, vertex6, vertex5, vertex8, vertex7);
            }
            case LINES -> {
                if(!sidesExcluded.contains(Direction.DOWN) && !sidesExcluded.contains(Direction.NORTH)) construct(buffer, vertex1, vertex2);
                if(!sidesExcluded.contains(Direction.DOWN) && !sidesExcluded.contains(Direction.EAST)) construct(buffer, vertex2, vertex3);
                if(!sidesExcluded.contains(Direction.DOWN) && !sidesExcluded.contains(Direction.SOUTH)) construct(buffer, vertex3, vertex4);
                if(!sidesExcluded.contains(Direction.DOWN) && !sidesExcluded.contains(Direction.WEST)) construct(buffer, vertex4, vertex1);
                if(!sidesExcluded.contains(Direction.NORTH) && !sidesExcluded.contains(Direction.WEST)) construct(buffer, vertex1, vertex5);
                if(!sidesExcluded.contains(Direction.NORTH) && !sidesExcluded.contains(Direction.EAST)) construct(buffer, vertex2, vertex6);
                if(!sidesExcluded.contains(Direction.SOUTH) && !sidesExcluded.contains(Direction.EAST)) construct(buffer, vertex3, vertex7);
                if(!sidesExcluded.contains(Direction.SOUTH) && !sidesExcluded.contains(Direction.WEST)) construct(buffer, vertex4, vertex8);
                if(!sidesExcluded.contains(Direction.UP) && !sidesExcluded.contains(Direction.NORTH)) construct(buffer, vertex5, vertex6);
                if(!sidesExcluded.contains(Direction.UP) && !sidesExcluded.contains(Direction.EAST)) construct(buffer, vertex6, vertex7);
                if(!sidesExcluded.contains(Direction.UP) && !sidesExcluded.contains(Direction.SOUTH)) construct(buffer, vertex7, vertex8);
                if(!sidesExcluded.contains(Direction.UP) && !sidesExcluded.contains(Direction.WEST)) construct(buffer, vertex8, vertex5);
            }
        }
    }
}

