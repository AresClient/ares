package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.forge.event.events.player.OnItemUsePass;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Mouse;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ObbyPlace", description = "Places obby on left click while using tools", category = Category.COMBAT)
public class ObbyPlace extends Module {
    private final Setting<Integer> placeDelay = register(new IntegerSetting("Place Delay", 5, 0, 20));

    private int nextPlace = 0;
    @EventHandler
    public EventListener<OnItemUsePass> onItemUsePass = new EventListener<>(event -> {
        if(nextPlace > MC.player.ticksExisted && nextPlace != 0) return;

        BlockPos pos = MC.objectMouseOver.getBlockPos().offset(MC.objectMouseOver.sideHit);
        if(pos == null) return;
        int item = MC.player.inventory.currentItem;
        int obby = InventoryUtils.findBlockInHotbar(Blocks.OBSIDIAN);
        if(obby == -1) return;
        MC.player.inventory.currentItem = obby;
        WorldUtils.placeBlockMainHand(pos);
        MC.player.inventory.currentItem = item;
        nextPlace = MC.player.ticksExisted + placeDelay.getValue();
    });

    @Override
    public void onTick() {
        //Reset tick delay on key up
        if(!Mouse.isButtonDown(1) && nextPlace > MC.player.ticksExisted)
            nextPlace = MC.player.ticksExisted;
    }
}