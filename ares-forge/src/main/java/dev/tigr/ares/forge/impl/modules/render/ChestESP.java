package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.forge.utils.RenderUtils;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ChestESP", description = "Highlight chests in render distance", category = Category.RENDER)
public class ChestESP extends Module {
    private static final Color CHEST = new Color(0, 0, 0.89f, 0.3f);
    private static final Color ENDER_CHEST = new Color(0.7f, 0, 0.7f, 0.3f);
    private static final Color DISPENSER = new Color(0.65f, 0.65f, 0.65f, 0.3f);
    private static final Color SHULKER = new Color(1, 0.45f, 0.55f, 0.3f);

    @Override
    public void onRender3d() {
        RenderUtils.prepare3d();

        MC.world.loadedTileEntityList.forEach(tileEntity -> {
            AxisAlignedBB bb = RenderUtils.getBoundingBox(tileEntity.getPos());
            if(tileEntity instanceof TileEntityChest) drawBox(bb, CHEST);
            else if(tileEntity instanceof TileEntityEnderChest) drawBox(bb, ENDER_CHEST);
            else if(tileEntity instanceof TileEntityDispenser || tileEntity instanceof TileEntityFurnace || tileEntity instanceof TileEntityHopper)
                drawBox(bb, DISPENSER);
            else if(tileEntity instanceof TileEntityShulkerBox) drawBox(bb, SHULKER);
        });

        RenderUtils.end3d();
    }

    private void drawBox(AxisAlignedBB bb, Color color) {
        RenderGlobal.renderFilledBox(bb, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        RenderGlobal.drawSelectionBoundingBox(bb, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
}
