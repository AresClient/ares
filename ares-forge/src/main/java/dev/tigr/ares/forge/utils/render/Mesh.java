package dev.tigr.ares.forge.utils.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.Vertex;
import dev.tigr.ares.forge.mixin.accessor.BufferBuilderAccessor;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Mesh implements Wrapper {
    private static void vertex(BufferBuilder buffer, Vertex vert) {
        buffer.pos((vert.x -MC.getRenderManager().viewerPosX), (vert.y -MC.getRenderManager().viewerPosY), (vert.z -MC.getRenderManager().viewerPosZ)).color(vert.r, vert.g, vert.b, vert.a).endVertex();
    }

    public static void construct(BufferBuilder buffer, Vertex... vertices) {
        for(Vertex vert: vertices) vertex(buffer, vert);
    }

    public static void triangle(BufferBuilder buffer, Vertex vertex1, Vertex vertex2, Vertex vertex3) {
        if(((BufferBuilderAccessor)buffer).getDrawMode() == GL_TRIANGLES) construct(buffer, vertex1, vertex2, vertex3);
        if(((BufferBuilderAccessor)buffer).getDrawMode() == GL_LINES) construct(buffer, vertex1, vertex2, vertex2, vertex3, vertex3, vertex1);
    }

    public static void quad(BufferBuilder buffer, Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4) {
        if(((BufferBuilderAccessor)buffer).getDrawMode() == GL_QUADS) construct(buffer, vertex1, vertex2, vertex3, vertex4);
        if(((BufferBuilderAccessor) buffer).getDrawMode() == GL_TRIANGLES) construct(buffer, vertex1, vertex2, vertex3, vertex1, vertex3, vertex4);
        if(((BufferBuilderAccessor) buffer).getDrawMode() == GL_LINES) construct(buffer, vertex1, vertex2, vertex2, vertex3, vertex3, vertex4, vertex4, vertex1);
    }

    public static void cube(BufferBuilder buffer, AxisAlignedBB box, Color colorA1, Color colorA2, Color colorA3, Color colorA4, Color colorB1, Color colorB2, Color colorB3, Color colorB4, EnumFacing... excludeSides) {
        cube(buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, colorA1, colorA2, colorA3, colorA4, colorB1, colorB2, colorB3, colorB4, excludeSides);
    }

    public static void cube(BufferBuilder buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color colorA1, Color colorA2, Color colorA3, Color colorA4, Color colorB1, Color colorB2, Color colorB3, Color colorB4, EnumFacing... excludeSides) {
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

    public static void cube(BufferBuilder buffer, Vertex vertex1, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5, Vertex vertex6, Vertex vertex7, Vertex vertex8, EnumFacing... excludeSides) {
        List<EnumFacing> sidesExcluded = Arrays.asList(excludeSides.clone());

        int drawMode = ((BufferBuilderAccessor)buffer).getDrawMode();
        if(drawMode == GL_TRIANGLES || drawMode == GL_QUADS) {
            /*  DOWN  */ if(!sidesExcluded.contains(EnumFacing.DOWN)) quad(buffer, vertex1, vertex2, vertex3, vertex4);
            /*  EAST  */ if(!sidesExcluded.contains(EnumFacing.EAST)) quad(buffer, vertex3, vertex2, vertex6, vertex7);
            /*  SOUTH */ if(!sidesExcluded.contains(EnumFacing.SOUTH)) quad(buffer, vertex7, vertex8, vertex4, vertex3);
            /*  WEST  */ if(!sidesExcluded.contains(EnumFacing.WEST)) quad(buffer, vertex8, vertex5, vertex1, vertex4);
            /*  NORTH */ if(!sidesExcluded.contains(EnumFacing.NORTH)) quad(buffer, vertex2, vertex1, vertex5, vertex6);
            /*  UP    */ if(!sidesExcluded.contains(EnumFacing.UP)) quad(buffer, vertex6, vertex5, vertex8, vertex7);
        } else {
            if(!sidesExcluded.contains(EnumFacing.DOWN) && !sidesExcluded.contains(EnumFacing.NORTH)) construct(buffer, vertex1, vertex2);
            if(!sidesExcluded.contains(EnumFacing.DOWN) && !sidesExcluded.contains(EnumFacing.EAST)) construct(buffer, vertex2, vertex3);
            if(!sidesExcluded.contains(EnumFacing.DOWN) && !sidesExcluded.contains(EnumFacing.SOUTH)) construct(buffer, vertex3, vertex4);
            if(!sidesExcluded.contains(EnumFacing.DOWN) && !sidesExcluded.contains(EnumFacing.WEST)) construct(buffer, vertex4, vertex1);
            if(!sidesExcluded.contains(EnumFacing.NORTH) && !sidesExcluded.contains(EnumFacing.WEST)) construct(buffer, vertex1, vertex5);
            if(!sidesExcluded.contains(EnumFacing.NORTH) && !sidesExcluded.contains(EnumFacing.EAST)) construct(buffer, vertex2, vertex6);
            if(!sidesExcluded.contains(EnumFacing.SOUTH) && !sidesExcluded.contains(EnumFacing.EAST)) construct(buffer, vertex3, vertex7);
            if(!sidesExcluded.contains(EnumFacing.SOUTH) && !sidesExcluded.contains(EnumFacing.WEST)) construct(buffer, vertex4, vertex8);
            if(!sidesExcluded.contains(EnumFacing.UP) && !sidesExcluded.contains(EnumFacing.NORTH)) construct(buffer, vertex5, vertex6);
            if(!sidesExcluded.contains(EnumFacing.UP) && !sidesExcluded.contains(EnumFacing.EAST)) construct(buffer, vertex6, vertex7);
            if(!sidesExcluded.contains(EnumFacing.UP) && !sidesExcluded.contains(EnumFacing.SOUTH)) construct(buffer, vertex7, vertex8);
            if(!sidesExcluded.contains(EnumFacing.UP) && !sidesExcluded.contains(EnumFacing.WEST)) construct(buffer, vertex8, vertex5);
        }
    }
}
