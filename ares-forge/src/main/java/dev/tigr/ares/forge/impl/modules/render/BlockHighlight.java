package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.forge.utils.render.RenderUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "BlockHighlight", description = "Customises your block highlight", category = Category.RENDER)
public class BlockHighlight extends Module {
    private final Setting<Boolean> onBreak = register(new BooleanSetting("Breaking Only", false));
    private final Setting<Float> red = register(new FloatSetting("Red", 1, 0, 1));
    private final Setting<Float> green = register(new FloatSetting("Green", 0, 0, 1));
    private final Setting<Float> blue = register(new FloatSetting("Blue", 0, 0, 1));
    private final Setting<Float> alpha = register(new FloatSetting("Alpha", 0.8f, 0, 1));
    private final Setting<Float> width = register(new FloatSetting("Width", 2, 0, 10));

    @Override
    public void onRender3d() {
        Color color = new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());

        if(onBreak.getValue() && !MC.gameSettings.keyBindAttack.isKeyDown()) return;

        BlockPos pos = MC.objectMouseOver.getBlockPos();

        if(pos != null && MC.world.getBlockState(pos).getMaterial().isOpaque()) {
            AxisAlignedBB bb = RenderUtils.getBoundingBox(MC.objectMouseOver.getBlockPos());

            RenderUtils.prepare3d();
            RenderUtils.cubeLines(bb, color, width.getValue());
            RenderUtils.end3d();
        }
    }
}
