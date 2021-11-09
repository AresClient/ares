package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.ares.forge.utils.HoleType;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.ares.forge.utils.entity.SelfUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.network.play.server.SPacketEntityStatus;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoSurround", description = "Automatically enable surround in certain circumstances", category = Category.COMBAT)
public class AutoSurround extends Module {
    private final Setting<Boolean> hole = register(new BooleanSetting("When in hole", true));
    private final Setting<Boolean> holeSnap = register(new BooleanSetting("Hole Center", false)) .setVisibility(hole::getValue);
    private final Setting<Boolean> pop = register(new BooleanSetting("On totem pop", false));
    private final Setting<Boolean> popSnap = register(new BooleanSetting("Totem Center", true)).setVisibility(pop::getValue);
    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(MC.player != null && event.getPacket() instanceof SPacketEntityStatus && pop.getValue()) {
            SPacketEntityStatus status = (SPacketEntityStatus) event.getPacket();
            if(status.getOpCode() == 35 && status.getEntity(MC.world) == MC.player) {
                Surround.INSTANCE.setEnabled(true);
                Surround.toggleCenter(popSnap.getValue());
            }
        }
    });

    @Override
    public void onTick() {
        if(WorldUtils.isHole(SelfUtils.getBlockPosCorrected()) != HoleType.NONE && !Surround.INSTANCE.getEnabled() && hole.getValue())
            Surround.INSTANCE.setEnabled(true);
        Surround.toggleCenter(holeSnap.getValue());
    }
}
