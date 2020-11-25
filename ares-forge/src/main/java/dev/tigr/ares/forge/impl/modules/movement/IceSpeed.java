package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;
import net.minecraft.init.Blocks;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "IceSpeed", description = "Move faster on ice", category = Category.MOVEMENT)
public class IceSpeed extends Module {
    private final Setting<Float> speed = register(new FloatSetting("Speed", 0.4f, 0.2f, 1));

    @Override
    @SuppressWarnings("deprecation")
    public void onTick() {
        Blocks.ICE.slipperiness = Blocks.PACKED_ICE.slipperiness = Blocks.FROSTED_ICE.slipperiness = speed.getValue();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onDisable() {
        Blocks.ICE.slipperiness = Blocks.PACKED_ICE.slipperiness = Blocks.FROSTED_ICE.slipperiness = 0.97f;
    }
}
