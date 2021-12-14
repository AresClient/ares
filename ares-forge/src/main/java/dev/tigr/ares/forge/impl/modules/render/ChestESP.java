package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.forge.mixin.accessor.BufferBuilderAccessor;
import dev.tigr.ares.forge.utils.Comparators;
import dev.tigr.ares.forge.utils.render.Mesh;
import dev.tigr.ares.forge.utils.render.RenderUtils;
import net.minecraft.block.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Tigermouthbear
 * rewrite by Makrennel 12/06/21
 */
@Module.Info(name = "ChestESP", description = "Highlight chests in render distance", category = Category.RENDER)
public class ChestESP extends Module {
    private final Setting<Boolean> chest = register(new BooleanSetting("Chests/Barrels", true));
    private final Setting<Boolean> eChest = register(new BooleanSetting("Ender Chests", true));
    private final Setting<Boolean> shulker = register(new BooleanSetting("Shulkers", true));
    private final Setting<Boolean> other = register(new BooleanSetting("Other", true));
    private final Setting<Boolean> lump = register(new BooleanSetting("Lump Together", true));
    private final Setting<ShouldDraw> shouldDraw = register(new EnumSetting<>("Draw", ShouldDraw.BOTH));

    private final Setting<ColorMenu> colorMenu = register(new EnumSetting<>("Color Menu", ColorMenu.CHEST));
    private final Setting<Float> cRed = register(new FloatSetting("C. Red", ColorMenu.CHEST.getFillColor().getRed(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.CHEST);
    private final Setting<Float> cGreen = register(new FloatSetting("C. Green", ColorMenu.CHEST.getFillColor().getGreen(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.CHEST);
    private final Setting<Float> cBlue = register(new FloatSetting("C. Blue", ColorMenu.CHEST.getFillColor().getBlue(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.CHEST);
    private final Setting<Float> cFAlpha = register(new FloatSetting("C. Fill Alpha", ColorMenu.CHEST.getFillColor().getAlpha(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.CHEST);
    private final Setting<Float> cLAlpha = register(new FloatSetting("C. Line Alpha", ColorMenu.CHEST.getLineColor().getAlpha(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.CHEST);

    private final Setting<Float> eRed = register(new FloatSetting("E. Red", ColorMenu.ENDER_CHEST.getFillColor().getRed(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.ENDER_CHEST);
    private final Setting<Float> eGreen = register(new FloatSetting("E. Green", ColorMenu.ENDER_CHEST.getFillColor().getGreen(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.ENDER_CHEST);
    private final Setting<Float> eBlue = register(new FloatSetting("E. Blue", ColorMenu.ENDER_CHEST.getFillColor().getBlue(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.ENDER_CHEST);
    private final Setting<Float> eFAlpha = register(new FloatSetting("E. Fill Alpha", ColorMenu.ENDER_CHEST.getFillColor().getAlpha(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.ENDER_CHEST);
    private final Setting<Float> eLAlpha = register(new FloatSetting("E. Line Alpha", ColorMenu.ENDER_CHEST.getLineColor().getAlpha(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.ENDER_CHEST);

    private final Setting<Float> oRed = register(new FloatSetting("D. Red", ColorMenu.OTHER.getFillColor().getRed(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.OTHER);
    private final Setting<Float> oGreen = register(new FloatSetting("D. Green", ColorMenu.OTHER.getFillColor().getGreen(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.OTHER);
    private final Setting<Float> oBlue = register(new FloatSetting("D. Blue", ColorMenu.OTHER.getFillColor().getBlue(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.OTHER);
    private final Setting<Float> oFAlpha = register(new FloatSetting("D. Fill Alpha", ColorMenu.OTHER.getFillColor().getAlpha(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.OTHER);
    private final Setting<Float> oLAlpha = register(new FloatSetting("D. Line Alpha", ColorMenu.OTHER.getLineColor().getAlpha(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.OTHER);

    private final Setting<Float> sRed = register(new FloatSetting("S. Red", ColorMenu.SHULKER.getFillColor().getRed(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.SHULKER);
    private final Setting<Float> sGreen = register(new FloatSetting("S. Green", ColorMenu.SHULKER.getFillColor().getGreen(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.SHULKER);
    private final Setting<Float> sBlue = register(new FloatSetting("S. Blue", ColorMenu.SHULKER.getFillColor().getBlue(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.SHULKER);
    private final Setting<Float> sFAlpha = register(new FloatSetting("S. Fill Alpha", ColorMenu.SHULKER.getFillColor().getAlpha(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.SHULKER);
    private final Setting<Float> sLAlpha = register(new FloatSetting("S. Line Alpha", ColorMenu.SHULKER.getLineColor().getAlpha(), 0, 1)).setVisibility(() -> colorMenu.getValue() == ColorMenu.SHULKER);

    private enum ColorMenu {
        CHEST(new Color(0, 0, 0.89f, 0.24f), 0.8f),
        ENDER_CHEST(new Color(0.7f, 0, 0.7f, 0.24f), 0.8f),
        OTHER(new Color(0.65f, 0.65f, 0.65f, 0.24f), 0.8f),
        SHULKER(new Color(1, 0.45f, 0.55f, 0.24f), 0.8f);

        private Color color;
        private float lineAlpha;

        ColorMenu(Color color, float lineAlpha) {
            this.color = color;
            this.lineAlpha = lineAlpha;
        }

        public void setColor(float r, float g, float b, float f, float l) {
            color = new Color(r, g, b, f);
            lineAlpha = l;
        }

        public Color getFillColor() {
            return color;
        }

        public Color getLineColor() {
            return color.withA(lineAlpha);
        }
    }

    private enum ShouldDraw { BOTH, QUADS, LINES }

    List<BlockPos> tilePoses = new ArrayList<>();
    AxisAlignedBB bb;
    EnumFacing[] ignoreEnumFacings = new EnumFacing[6];

    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();

    @Override
    public void onTick() {
        ColorMenu.CHEST.setColor(cRed.getValue(), cGreen.getValue(), cBlue.getValue(), cFAlpha.getValue(), cLAlpha.getValue());
        ColorMenu.ENDER_CHEST.setColor(eRed.getValue(), eGreen.getValue(), eBlue.getValue(), eFAlpha.getValue(), eLAlpha.getValue());
        ColorMenu.OTHER.setColor(oRed.getValue(), oGreen.getValue(), oBlue.getValue(), oFAlpha.getValue(), oLAlpha.getValue());
        ColorMenu.SHULKER.setColor(sRed.getValue(), sGreen.getValue(), sBlue.getValue(), sFAlpha.getValue(), sLAlpha.getValue());

        tilePoses.clear();
        for(TileEntity blockEntity: MC.world.loadedTileEntityList)
            tilePoses.add(blockEntity.getPos());
        tilePoses.sort(Collections.reverseOrder(Comparators.blockDistance));
    }

    @Override
    public void onRender3d() {
        RenderUtils.prepare3d();

        switch(shouldDraw.getValue()) {
            case QUADS:
                drawQuads();
                break;
            case LINES:
                drawLines();
                break;
            default:
                drawQuads();
                drawLines();
        }

        RenderUtils.end3d();
    }

    private void drawQuads() {
        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        fillBuffer();
        tessellator.draw();
    }

    private void drawLines() {
        GlStateManager.disableCull();
        GlStateManager.glLineWidth(2);

        buffer.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        fillBuffer();
        tessellator.draw();

        GlStateManager.glLineWidth(1);
        GlStateManager.enableCull();
    }

    private void fillBuffer() {
        for(BlockPos blockPos: tilePoses) {
            if(lump.getValue()) {
                bb = new AxisAlignedBB(blockPos);
                for(EnumFacing direction: EnumFacing.values()) {
                    ignoreEnumFacings[direction.getIndex()] = null;
                    if(shouldLump(blockPos.offset(direction)))
                        ignoreEnumFacings[direction.getIndex()] = direction;
                }
            } else {
                bb = RenderUtils.getBoundingBox(blockPos);
                for(int i = 0; i < 6; i++) ignoreEnumFacings[i] = null;
            }

            if(bb == null) continue;

            Block block = MC.world.getBlockState(blockPos).getBlock();
            if(chest.getValue() && block instanceof BlockChest) {
                EnumFacing stretch = null;
                if(!lump.getValue()) {
                    TileEntity tileEntity = MC.world.getTileEntity(blockPos);
                    if(tileEntity instanceof TileEntityChest) {
                        TileEntityChest chestEntity = (TileEntityChest) tileEntity;
                        if(chestEntity.adjacentChestZNeg != null) stretch = EnumFacing.NORTH;
                        if(chestEntity.adjacentChestZPos != null) stretch = EnumFacing.SOUTH;
                        if(chestEntity.adjacentChestXNeg != null) stretch = EnumFacing.WEST;
                        if(chestEntity.adjacentChestZNeg != null) stretch = EnumFacing.NORTH;
                    }
                }

                if(stretch != null) bb = doubleChest(bb, stretch);
                else {
                    int ignoring = 0;
                    for(int i = 0; i < 6; i++) if(ignoreEnumFacings[i] != null) ignoring++;
                    if(ignoring == 0) bb = RenderUtils.getBoundingBox(blockPos);
                }

                if(bb == null) continue;

                addAxisAlignedBBWithColor(ColorMenu.CHEST);
            }
            else if(eChest.getValue() && block instanceof BlockEnderChest) {
                int ignoring = 0;
                for(int i = 0; i < 6; i++) if(ignoreEnumFacings[i] != null) ignoring++;
                if(ignoring == 0) bb = RenderUtils.getBoundingBox(blockPos);

                if(bb == null) continue;

                addAxisAlignedBBWithColor(ColorMenu.ENDER_CHEST);
            }
            else if(other.getValue() && (block instanceof BlockDispenser || block instanceof BlockFurnace || block instanceof BlockHopper)) {
                addAxisAlignedBBWithColor(ColorMenu.OTHER);
            }
            else if(shulker.getValue() && block instanceof BlockShulkerBox) {
                addAxisAlignedBBWithColor(ColorMenu.SHULKER);
            }
        }
    }

    private boolean shouldLump(BlockPos blockPos) {
        Block block = MC.world.getBlockState(blockPos).getBlock();
        return
                (chest.getValue() && block instanceof BlockChest)
                        || (eChest.getValue() && block instanceof BlockEnderChest)
                        || (shulker.getValue() && block instanceof BlockShulkerBox)
                        || (other.getValue() && (block instanceof BlockDispenser || block instanceof BlockFurnace || block instanceof BlockHopper));
    }

    private void addAxisAlignedBBWithColor(ColorMenu colorMenu) {
        Color drawColor;
        if(((BufferBuilderAccessor) buffer).getDrawMode() == GL_QUADS || ((BufferBuilderAccessor) buffer).getDrawMode() == GL_TRIANGLES) drawColor = colorMenu.getFillColor();
        else drawColor = colorMenu.getLineColor();

        Mesh.cube(buffer, bb, drawColor, drawColor, drawColor, drawColor, drawColor, drawColor, drawColor, drawColor, ignoreEnumFacings);
    }

    private AxisAlignedBB doubleChest(AxisAlignedBB box, EnumFacing face) {
        switch(face) {
            case EAST:
                return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX + 0.94, box.maxY, box.maxZ);
            case SOUTH:
                return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ + 0.94);
            default:
                return null;
        }
    }
}
