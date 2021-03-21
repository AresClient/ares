package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.utils.HoleType;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

/**
 * @author Tigermouthbear
 * ported to Fabric by Hoosiers
 */
@Module.Info(name = "AutoSurround", description = "Automatically enable surround in certain circumstances", category = Category.COMBAT)
public class AutoSurround extends Module {
    private final Setting<Boolean> hole = register(new BooleanSetting("When in hole", true));
    private final Setting<Boolean> holeSnap = register(new BooleanSetting("Hole Center", false)) .setVisibility(hole::getValue);
    private final Setting<Integer> holeDelay = register(new IntegerSetting("Hole D.(ms)", 250, 0, 1000)).setVisibility(hole::getValue);
    private final Setting<Boolean> pop = register(new BooleanSetting("On totem pop", false));
    private final Setting<Boolean> popSnap = register(new BooleanSetting("Totem Center", true)).setVisibility(pop::getValue);

    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(MC.player != null && event.getPacket() instanceof EntityStatusS2CPacket && pop.getValue()) {
            EntityStatusS2CPacket status = (EntityStatusS2CPacket) event.getPacket();
            if(status.getStatus() == 35 && status.getEntity(MC.world) == MC.player) {
                Surround.INSTANCE.setEnabled(true);
                Surround.toggleCenter(popSnap.getValue());
            }
        }
    });

    @Override
    public void onTick() {
        if (WorldUtils.isHole(MC.player.getBlockPos()) != HoleType.NONE && !Surround.INSTANCE.getEnabled() && hole.getValue()) {
            Surround.INSTANCE.setEnabled(true);
            Surround.toggleCenter(holeSnap.getValue());
            Surround.setSurroundWait(holeDelay.getValue());
        }
    }
}
