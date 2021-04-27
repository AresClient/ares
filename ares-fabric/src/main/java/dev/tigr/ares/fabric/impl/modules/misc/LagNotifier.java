package dev.tigr.ares.fabric.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author nwroot
 */
@Module.Info(name = "LagNotifier", description = "Notifies if we stopped receiving server packets", category = Category.MISC)
public class LagNotifier extends Module {
    private final Setting<Integer> maxTicks = register(new IntegerSetting("Max Ticks", 40, 10, 100));
    private final Setting<Boolean> reconnectMessage = register(new BooleanSetting("Reconnect Message", true));
    
    private int currentTick = 0;
    private int lastPacketTick = 0;
    private boolean serverLagging = false;
    
    @Override
    public void onTick() {
        currentTick++;
        if(currentTick - lastPacketTick >= maxTicks.getValue() && !serverLagging) {
            serverLagging = true;
            UTILS.printMessage("Server is not responding!");
        }
        
    }
    
    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(serverLagging && reconnectMessage.getValue()) {
            UTILS.printMessage("Received a server packet after " + Integer.toString(currentTick - lastPacketTick) + " client ticks");
        }
        lastPacketTick = currentTick;
        serverLagging = false;
    });
}
