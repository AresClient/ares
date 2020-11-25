package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.fabric.utils.HoleType;
import dev.tigr.ares.fabric.utils.RenderUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

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
        holes.clear();
        for(BlockPos pos: WorldUtils.getAllInBox((int) MC.player.getPos().x - holeRadius.getValue(), (int) MC.player.getPos().y - holeRadius.getValue(), (int) MC.player.getPos().z - holeRadius.getValue(), (int) MC.player.getPos().x + holeRadius.getValue(), (int) MC.player.getPos().y + holeRadius.getValue(), (int) MC.player.getPos().z + holeRadius.getValue())) {
            if(pos.getY() > maxY.getValue()) continue;

            HoleType type = WorldUtils.isHole(pos);

            if((type == HoleType.OTHER && mode.getValue() != Mode.ALL) || type == HoleType.NONE) continue;

            holes.put(pos, type);
        }
    }

    @Override
    public void onRender3d() {
        RenderUtils.glBegin();

        for(BlockPos pos: holes.keySet()) {
            Box bb = new Box(pos);

            switch(holes.get(pos)) {
                case BEDROCK:
                    //green
                    RenderUtils.renderFilledBox(bb, 0, 0.93f, 0, 0.5f);
                    RenderUtils.renderSelectionBoundingBox(bb, 0, 0.55f, 0, 0.5f);
                    break;

                case OBBY:
                    //yellow
                    RenderUtils.renderFilledBox(bb, 0.93f, 0.93f, 0, 0.5f);
                    RenderUtils.renderSelectionBoundingBox(bb, 0.93f, 0.93f, 0, 0.5f);
                    break;

                default:
                    RenderUtils.renderFilledBox(bb, 1, 1, 1, 0.5f);
                    RenderUtils.renderSelectionBoundingBox(bb, 1, 1, 1, 0.5f);
            }
        }

        RenderUtils.glEnd();
    }

    enum Mode {
        OBBYANDBEDROCK,
        ALL
    }
}
