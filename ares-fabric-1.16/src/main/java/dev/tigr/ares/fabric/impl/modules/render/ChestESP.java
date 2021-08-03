package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.utils.RenderUtils;
import net.minecraft.block.entity.*;
import net.minecraft.util.math.Box;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 8/28/20
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
        for(BlockEntity tileEntity: MC.world.blockEntities) {
            Box bb = RenderUtils.getBoundingBox(tileEntity.getPos());
            if(bb == null) continue;
            if(tileEntity instanceof ChestBlockEntity || tileEntity instanceof BarrelBlockEntity)
                RenderUtils.renderBlockNoPrepare(tileEntity.getPos(), CHEST, CHEST);
            else if(tileEntity instanceof EnderChestBlockEntity)
                RenderUtils.renderBlockNoPrepare(tileEntity.getPos(), ENDER_CHEST, ENDER_CHEST);
            else if(tileEntity instanceof DispenserBlockEntity || tileEntity instanceof FurnaceBlockEntity || tileEntity instanceof HopperBlockEntity || tileEntity instanceof BlastFurnaceBlockEntity)
                RenderUtils.renderBlockNoPrepare(tileEntity.getPos(), DISPENSER, DISPENSER);
            else if(tileEntity instanceof ShulkerBoxBlockEntity)
                RenderUtils.renderBlockNoPrepare(tileEntity.getPos(), SHULKER, SHULKER);
        }
        RenderUtils.end3d();
    }
}
