package dev.tigr.ares.fabric.utils.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.mixin.accessors.BufferBuilderAccessor;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mesh implements Wrapper {
    public static void construct(BufferBuilder buffer, Matrix4f model, Vertex... vertices) {
        for(Vertex vert: vertices) {
            buffer.vertex(model, (float)(vert.x -MC.gameRenderer.getCamera().getPos().x), (float)(vert.y -MC.gameRenderer.getCamera().getPos().y), (float)(vert.z -MC.gameRenderer.getCamera().getPos().z)).color(vert.r, vert.g, vert.b, vert.a).next();
        }
    }

    public static void construct(BufferBuilder buffer, Matrix4f model, Matrix3f normal, Vec3f normalVec, Vertex... vertices) {
        for(Vertex vert: vertices) {
            buffer.vertex(model, (float)(vert.x -MC.gameRenderer.getCamera().getPos().x), (float)(vert.y -MC.gameRenderer.getCamera().getPos().y), (float)(vert.z -MC.gameRenderer.getCamera().getPos().z)).color(vert.r, vert.g, vert.b, vert.a).normal(normal, normalVec.getX(), normalVec.getY(), normalVec.getZ()).next();
        }
    }

    public static void triangle(BufferBuilder buffer, Matrix4f model, Matrix3f normal, Vec3f normalVec, Vertex vertex1, Vertex vertex2, Vertex vertex3) {
        switch(((BufferBuilderAccessor)buffer).getDrawMode()) {
            case TRIANGLES -> construct(buffer, model, vertex1, vertex2, vertex3);
            case LINES -> construct(buffer, model, normal, normalVec, vertex1, vertex2, vertex2, vertex3, vertex3, vertex1);
        }
    }

    public static void quad(BufferBuilder buffer, Matrix4f model, Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4) {
        quad(buffer, model, null, null, vertex1, vertex2, vertex3, vertex4);
    }

    public static void quad(BufferBuilder buffer, Matrix4f model, Matrix3f normal, Vec3f normalVec, Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4) {
        switch(((BufferBuilderAccessor)buffer).getDrawMode()) {
            case QUADS -> construct(buffer, model, vertex1, vertex2, vertex3, vertex4);
            case TRIANGLES -> construct(buffer, model, vertex1, vertex2, vertex3, vertex1, vertex3, vertex4);
            case LINES -> construct(buffer, model, normal, normalVec, vertex1, vertex2, vertex2, vertex3, vertex3, vertex4, vertex4, vertex1);
        }
    }

    public static void cube(BufferBuilder buffer, Matrix4f model, Box box, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, Direction... excludeSides) {
        cube(buffer, model, null, null, box, color1, color2, color3, color4, color5, color6, color7, color8, excludeSides);
    }

    public static void cube(BufferBuilder buffer, Matrix4f model, Matrix3f normal, Vec3f normalVec, Box box, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, Direction... excludeSides) {
        cube(buffer, model, normal, normalVec, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color1, color2, color3, color4, color5, color6, color7, color8, excludeSides);
    }

    public static void cube(BufferBuilder buffer, Matrix4f model, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, Direction... excludeSides) {
        cube(buffer, model, null, null, minX, minY, minZ, maxX, maxY, maxZ, color1, color2, color3, color4, color5, color6, color7, color8, excludeSides);
    }

    public static void cube(BufferBuilder buffer, Matrix4f model, Matrix3f normal, Vec3f normalVec, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color color1, Color color2, Color color3, Color color4, Color color5, Color color6, Color color7, Color color8, Direction... excludeSides) {
        Vertex
                vertDNW = new Vertex(minX, minY, minZ, color1),
                vertDNE = new Vertex(maxX, minY, minZ, color2),
                vertDSE = new Vertex(maxX, minY, maxZ, color3),
                vertDSW = new Vertex(minX, minY, maxZ, color4),
                vertUNW = new Vertex(minX, maxY, minZ, color5),
                vertUNE = new Vertex(maxX, maxY, minZ, color6),
                vertUSE = new Vertex(maxX, maxY, maxZ, color7),
                vertUSW = new Vertex(minX, maxY, maxZ, color8);

        cube(buffer, model, normal, normalVec, vertDNW, vertDNE, vertDSE, vertDSW, vertUNW, vertUNE, vertUSE, vertUSW, excludeSides);
    }

    public static void cube(BufferBuilder buffer, Matrix4f model, Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5, Vertex vertex6, Vertex vertex7, Vertex vertex8, Direction... excludeSides) {
        cube(buffer, model, null, null, vertex1, vertex2, vertex3, vertex4, vertex5, vertex6, vertex7, vertex8, excludeSides);
    }

    public static void cube(BufferBuilder buffer, Matrix4f model, Matrix3f normal, Vec3f normalVec, Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5, Vertex vertex6, Vertex vertex7, Vertex vertex8, Direction... excludeSides) {
        List<Direction> sidesExcluded = Arrays.asList(excludeSides.clone());

        switch(((BufferBuilderAccessor)buffer).getDrawMode()) {
            case QUADS, TRIANGLES -> {
                /*  DOWN  */if(!sidesExcluded.contains(Direction.DOWN)) quad(buffer, model, vertex1, vertex2, vertex3, vertex4);
                /*  EAST  */if(!sidesExcluded.contains(Direction.EAST)) quad(buffer, model, vertex3, vertex2, vertex6, vertex7);
                /*  SOUTH */if(!sidesExcluded.contains(Direction.SOUTH)) quad(buffer, model, vertex7, vertex8, vertex4, vertex3);
                /*  WEST  */if(!sidesExcluded.contains(Direction.WEST)) quad(buffer, model, vertex8, vertex5, vertex1, vertex4);
                /*  NORTH */if(!sidesExcluded.contains(Direction.NORTH)) quad(buffer, model, vertex2, vertex1, vertex5, vertex6);
                /*  UP    */if(!sidesExcluded.contains(Direction.UP)) quad(buffer, model, vertex6, vertex5, vertex8, vertex7);
            }
            case LINES -> construct(buffer, model, normal, normalVec,
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
        }
    }
}
