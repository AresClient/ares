package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.forge.utils.render.RenderUtils;
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
            if(tileEntity instanceof TileEntityChest)
                RenderUtils.cube(bb, CHEST, CHEST);
            else if(tileEntity instanceof TileEntityEnderChest)
                RenderUtils.cube(bb, ENDER_CHEST, ENDER_CHEST);
            else if(tileEntity instanceof TileEntityDispenser || tileEntity instanceof TileEntityFurnace || tileEntity instanceof TileEntityHopper)
                RenderUtils.cube(bb, DISPENSER, DISPENSER);
            else if(tileEntity instanceof TileEntityShulkerBox)
                RenderUtils.cube(bb, SHULKER, SHULKER);
        });
        RenderUtils.end3d();
    }
}
