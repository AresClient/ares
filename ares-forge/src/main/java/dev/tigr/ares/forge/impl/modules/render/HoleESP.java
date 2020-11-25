package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.forge.utils.HoleType;
import dev.tigr.ares.forge.utils.RenderUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "HoleESP", description = "Boxes holes near the client for crystal pvp", category = Category.RENDER)
public class HoleESP extends Module {
    private final HashMap<BlockPos, HoleType> holes = new HashMap<>();
    private final Setting<Mode> mode = register(new EnumSetting<>("Mode", Mode.OBBYANDBEDROCK));
    private final Setting<Integer> holeRadius = register(new IntegerSetting("Hole Radius", 10, 1, 50));
    private final Setting<Integer> delay = register(new IntegerSetting("Delay", 4, 1, 10));
    private final Setting<Integer> maxY = register(new IntegerSetting("MaxY", 256, 5, 256));

    @Override
    public void onTick() {
        if(MC.player.ticksExisted % delay.getValue() != 0) return;

        holes.clear();
        for(BlockPos pos: BlockPos.getAllInBox((int) MC.player.getPositionVector().x - holeRadius.getValue(), (int) MC.player.getPositionVector().y - holeRadius.getValue(), (int) MC.player.getPositionVector().z - holeRadius.getValue(), (int) MC.player.getPositionVector().x + holeRadius.getValue(), (int) MC.player.getPositionVector().y + holeRadius.getValue(), (int) MC.player.getPositionVector().z + holeRadius.getValue())) {
            if(pos.getY() > maxY.getValue()) continue;

            HoleType type = WorldUtils.isHole(pos);

            if((type == HoleType.OTHER && mode.getValue() != Mode.ALL) || type == HoleType.NONE) continue;

            holes.put(pos, type);
        }
    }

    @Override
    public void onRender3d() {
        if(holes != null) {
            RenderUtils.prepare3d();

            for(BlockPos pos: holes.keySet()) {
                HoleType type = holes.get(pos);
                AxisAlignedBB bb = RenderUtils.getBoundingBox(pos);

                switch(type) {
                    case BEDROCK:
                        //green
                        RenderGlobal.renderFilledBox(bb, 0, 0.93f, 0, 0.2f);
                        RenderGlobal.drawSelectionBoundingBox(bb, 0, 0.55f, 0, 0.2f);
                        break;

                    case OBBY:
                        //yellow
                        RenderGlobal.renderFilledBox(bb, 0.93f, 0.93f, 0, 0.2f);
                        RenderGlobal.drawSelectionBoundingBox(bb, 0.93f, 0.93f, 0, 0.2f);
                        break;

                    default:
                        RenderGlobal.renderFilledBox(bb, 1, 1, 1, 0.2f);
                        RenderGlobal.drawSelectionBoundingBox(bb, 1, 1, 1, 0.2f);
                }
            }

            RenderUtils.end3d();
        }
    }

    enum Mode {
        OBBYANDBEDROCK,
        ALL
    }
}
