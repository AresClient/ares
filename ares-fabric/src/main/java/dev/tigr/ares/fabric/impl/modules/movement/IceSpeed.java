package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import dev.tigr.ares.fabric.mixin.accessors.AbstractBlockAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.Arrays;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "IceSpeed", description = "Move faster on ice", category = Category.MOVEMENT)
public class IceSpeed extends Module {
    private final Setting<Float> speed = register(new FloatSetting("Speed", 0.4f, 0.2f, 1));

    @Override
    public void onTick() {
        setSlipperiness(speed.getValue());
    }

    @Override
    public void onDisable() {
        setSlipperiness(0.98f);
        ((AbstractBlockAccessor) Blocks.BLUE_ICE).setSlipperiness(0.989f);
    }

    private void setSlipperiness(float speed) {
        for(Block block: Arrays.asList(Blocks.ICE, Blocks.PACKED_ICE, Blocks.FROSTED_ICE, Blocks.BLUE_ICE)) {
            ((AbstractBlockAccessor) block).setSlipperiness(speed);
        }
    }
}
