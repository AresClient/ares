package dev.tigr.ares.fabric.impl.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.fabric.utils.RenderUtils;
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

    @Override
    public void onRender3d() {
        if((onBreak.getValue() && !MC.options.keyAttack.isPressed()) || MC.crosshairTarget == null) return;

        BlockPos pos = MC.crosshairTarget.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) MC.crosshairTarget).getBlockPos() : null;

        if(pos != null) {
            Box bb = RenderUtils.getBoundingBox(pos);

            if(bb == null) return;

            RenderUtils.glBegin();
            RenderSystem.lineWidth(width.getValue());
            RenderUtils.renderSelectionBoundingBox(bb, red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());
            RenderUtils.glEnd();
        }
    }
}
