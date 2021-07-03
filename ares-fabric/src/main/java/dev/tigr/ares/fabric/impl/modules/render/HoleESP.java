package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.impl.modules.render.HoleESP.Mode;
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

    private final Setting<Boolean> bedrockColor = register(new BooleanSetting("Bedrock Color", false));
    private final Setting<Float> bedrockRed = register(new FloatSetting("BR.Red", 0, 0, 1)).setVisibility(bedrockColor::getValue);
    private final Setting<Float> bedrockGreen = register(new FloatSetting("BR.Green", 0.93f, 0, 1)).setVisibility(bedrockColor::getValue);
    private final Setting<Float> bedrockBlue = register(new FloatSetting("BR.Blue", 0, 0, 1)).setVisibility(bedrockColor::getValue);
    private final Setting<Float> bedrockFillAlpha = register(new FloatSetting("BR.Fill", 0.3f, 0, 1)).setVisibility(bedrockColor::getValue);
    private final Setting<Float> bedrockBoxAlpha = register(new FloatSetting("BR.Line", 0.69f, 0, 1)).setVisibility(bedrockColor::getValue);

    private final Setting<Boolean> obbyColor = register(new BooleanSetting("Obsidian Color", false)).setVisibility(() -> mode.getValue() != Mode.BEDROCKONLY);
    private final Setting<Float> obbyRed = register(new FloatSetting("OB.Red", 0.93f, 0, 1)).setVisibility(() -> obbyColor.getValue() && mode.getValue() != Mode.BEDROCKONLY);
    private final Setting<Float> obbyGreen = register(new FloatSetting("OB.Green", 0.93f, 0, 1)).setVisibility(() -> obbyColor.getValue() && mode.getValue() != Mode.BEDROCKONLY);
    private final Setting<Float> obbyBlue = register(new FloatSetting("OB.Blue", 0, 0, 1)).setVisibility(() -> obbyColor.getValue() && mode.getValue() != Mode.BEDROCKONLY);
    private final Setting<Float> obbyFillAlpha = register(new FloatSetting("OB.Fill", 0.3f, 0, 1)).setVisibility(() -> obbyColor.getValue() && mode.getValue() != Mode.BEDROCKONLY);
    private final Setting<Float> obbyBoxAlpha = register(new FloatSetting("OB.Line", 0.69f, 0, 1)).setVisibility(() -> obbyColor.getValue() && mode.getValue() != Mode.BEDROCKONLY);

    private final Setting<Boolean> otherColor = register(new BooleanSetting("Other Color", false)).setVisibility(() -> mode.getValue() == Mode.ALL);
    private final Setting<Float> otherRed = register(new FloatSetting("OTH.Red", 1, 0, 1)).setVisibility(() -> otherColor.getValue() && mode.getValue() == Mode.ALL);
    private final Setting<Float> otherGreen = register(new FloatSetting("OTH.Green", 1, 0, 1)).setVisibility(() -> otherColor.getValue() && mode.getValue() == Mode.ALL);
    private final Setting<Float> otherBlue = register(new FloatSetting("OTH.Blue", 1, 0, 1)).setVisibility(() -> otherColor.getValue() && mode.getValue() == Mode.ALL);
    private final Setting<Float> otherFillAlpha = register(new FloatSetting("OTH.Fill", 0.3f, 0, 1)).setVisibility(() -> otherColor.getValue() && mode.getValue() == Mode.ALL);
    private final Setting<Float> otherBoxAlpha = register(new FloatSetting("OTH.Line", 0.69f, 0, 1)).setVisibility(() -> otherColor.getValue() && mode.getValue() == Mode.ALL);

    @Override
    public void onTick() {
        holes.clear();
        for(BlockPos pos: WorldUtils.getAllInBox(
                (int) MC.player.getPos().x - holeRadius.getValue(),
                (int) MC.player.getPos().y - holeRadius.getValue(),
                (int) MC.player.getPos().z - holeRadius.getValue(),
                (int) MC.player.getPos().x + holeRadius.getValue(),
                (int) MC.player.getPos().y + holeRadius.getValue(),
                (int) MC.player.getPos().z + holeRadius.getValue()
        )) {
            if(pos.getY() > maxY.getValue()) continue;

            HoleType type = WorldUtils.isHole(pos);

            if((type == HoleType.OTHER && mode.getValue() != Mode.ALL)
                    || type == HoleType.NONE
                    || (type == HoleType.OBBY && mode.getValue() == Mode.BEDROCKONLY))
                continue;

            holes.put(pos, type);
        }
    }

    @Override
    public void onRender3d() {
        Color bedrockFillColor = new Color(
                bedrockRed.getValue(),
                bedrockGreen.getValue(),
                bedrockBlue.getValue(),
                bedrockFillAlpha.getValue()
        );
        Color bedrockOutlineColor = new Color(
                bedrockRed.getValue(),
                bedrockGreen.getValue(),
                bedrockBlue.getValue(),
                bedrockBoxAlpha.getValue()
        );
        Color obbyFillColor = new Color(
                obbyRed.getValue(),
                obbyGreen.getValue(),
                obbyBlue.getValue(),
                obbyFillAlpha.getValue()
        );
        Color obbyOutlineColor = new Color(
                obbyRed.getValue(),
                obbyGreen.getValue(),
                obbyBlue.getValue(),
                obbyBoxAlpha.getValue()
        );
        Color otherFillColor = new Color(
                otherRed.getValue(),
                otherGreen.getValue(),
                otherBlue.getValue(),
                otherFillAlpha.getValue()
        );
        Color otherOutlineColor = new Color(
                otherRed.getValue(),
                otherGreen.getValue(),
                otherBlue.getValue(),
                otherBoxAlpha.getValue()
        );
        RenderUtils.prepare3d();

        for(BlockPos pos: holes.keySet()) {
            Box bb = new Box(pos).offset(
                    -MC.gameRenderer.getCamera().getPos().x,
                    -MC.gameRenderer.getCamera().getPos().y,
                    -MC.gameRenderer.getCamera().getPos().z
            );

            switch(holes.get(pos)) {
                case BEDROCK:
                    //green
                    RenderUtils.renderBlockNoPrepare(bb, bedrockFillColor, bedrockOutlineColor);
                    break;

                case OBBY:
                    //yellow
                    RenderUtils.renderBlockNoPrepare(bb, obbyFillColor, obbyOutlineColor);
                    break;

                default:
                    RenderUtils.renderBlockNoPrepare(bb, otherFillColor, otherOutlineColor);
            }
        }

        RenderUtils.end3d();
    }

    enum Mode {
        BEDROCKONLY,
        OBBYANDBEDROCK,
        ALL
    }
}
