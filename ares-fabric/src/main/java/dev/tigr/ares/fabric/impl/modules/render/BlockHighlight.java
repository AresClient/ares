package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.utils.render.RenderUtils;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

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
    private final Setting<Float> expand = register(new FloatSetting("Scale", 0f, -0.12f, 0.06f));

    @Override
    public void onRender3d() {
        Color color = new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());

        if((onBreak.getValue() && !MC.options.keyAttack.isPressed()) || MC.crosshairTarget == null) return;

        BlockPos pos = MC.crosshairTarget.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) MC.crosshairTarget).getBlockPos() : null;

        if(pos != null) {
            Box bb = RenderUtils.getBoundingBox(pos);
            if(bb == null) return;

            RenderUtils.prepare3d();
            RenderUtils.cubeLines(bb, color, width.getValue());
            RenderUtils.end3d();
        }
    }
}
