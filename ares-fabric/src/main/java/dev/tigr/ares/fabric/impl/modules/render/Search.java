package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.ListSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.fabric.utils.RenderUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Search", description = "Allows you to see certain blocks through walls", category = Category.RENDER)
public class Search extends Module {
    private static Search INSTANCE;

    private final Setting<List<Integer>> list = register(new ListSetting<>("Blocks-fabric", Arrays.asList(
            Registry.BLOCK.getRawId(Blocks.SPAWNER),
            Registry.BLOCK.getRawId(Blocks.NETHER_PORTAL),
            Registry.BLOCK.getRawId(Blocks.END_PORTAL_FRAME)
    )));

    private final Setting<Integer> delay = register(new IntegerSetting("Delay", 60, 10, 60));
    private final Setting<Integer> radius = register(new IntegerSetting("Radius", 80, 30, 150));
    private final Setting<Boolean> tracers = register(new BooleanSetting("Tracers", false));
    private final Setting<Float> red = register(new FloatSetting("Red", 0.54f, 0, 1));
    private final Setting<Float> green = register(new FloatSetting("Green", 0.03f, 0, 1));
    private final Setting<Float> blue = register(new FloatSetting("Blue", 0.03f, 0, 1));
    private final Setting<Boolean> rainbow = register(new BooleanSetting("Rainbow", false));

    private final List<BlockPos> blocks = new ArrayList<>();

    public Search() {
        INSTANCE = this;
    }

    public static boolean add(Block block) {
        int id = Registry.BLOCK.getRawId(block);

        if(INSTANCE.list.getValue().contains(id)) return false;

        INSTANCE.list.getValue().add(id);
        return true;
    }

    public static boolean del(Block block) {
        int id = Registry.BLOCK.getRawId(block);

        if(!INSTANCE.list.getValue().contains(id)) return false;

        INSTANCE.list.getValue().remove(Integer.valueOf(id));
        return true;
    }

    public static List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>();
        for(int id: INSTANCE.list.getValue()) blocks.add(Registry.BLOCK.get(id));

        return blocks;
    }

    private int tick = 0;
    @Override
    public void onTick() {
        if(tick++ % delay.getValue() == 0) {
            EXECUTOR.execute(() -> {
                List<BlockPos> list = new ArrayList<>();
                List<Block> blocksToFind = getBlocks();
                if(blocksToFind.isEmpty()) {
                    blocks.clear();
                    return;
                }
                 for(BlockPos pos: WorldUtils.getAllInBox((int) MC.player.getPos().x - radius.getValue(), 0, (int) MC.player.getPos().z - radius.getValue(), (int) MC.player.getPos().x + radius.getValue(), 256, (int) MC.player.getPos().z + radius.getValue())) {
                    if(blocksToFind.contains(MC.world.getBlockState(pos).getBlock())) list.add(pos);
                }
                blocks.clear();
                blocks.addAll(list);
            });
        }
    }

    @Override
    public void onRender3d() {
        if(blocks != null) {
            RenderUtils.glBegin();

            Color color;
            if(!rainbow.getValue()) color = new Color(red.getValue(), green.getValue(), blue.getValue(), 0.3f);
            else color = IRenderer.rainbow().setA(0.3f);

            for(BlockPos pos: new ArrayList<>(blocks)) {
                Box bb = RenderUtils.getBoundingBox(pos);
                RenderUtils.renderFilledBox(bb, color);
                RenderUtils.renderSelectionBoundingBox(bb, color);
            }

            RenderUtils.glEnd();

            if(tracers.getValue()) {
                RenderUtils.glBegin();
                for(BlockPos pos: new ArrayList<>(blocks)) RenderUtils.drawTracer(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Color.WHITE);
                RenderUtils.glEnd();
            }
        }
    }
}
