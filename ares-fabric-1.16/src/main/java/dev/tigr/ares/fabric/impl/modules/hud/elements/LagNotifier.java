package dev.tigr.ares.fabric.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.impl.modules.hud.HudElement;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author nwroot
 */
@Module.Info(name = "LagNotifier", description = "Notifies if the server stopped sending packets", category = Category.HUD)
public class LagNotifier extends HudElement {
    private final Setting<Integer> maxTicks = register(new IntegerSetting("Max Ticks", 40, 10, 100));
    private final Setting<Boolean> hudDraw = register(new BooleanSetting("Draw Overlay", true));
    private final Setting<Boolean> chatMessage = register(new BooleanSetting("Chat Message", true));
    private final Setting<Boolean> reconnectMessage = register(new BooleanSetting("Reconnect Message", true));
    
    private int currentTick = 0;
    private int lastPacketTick = 0;
    private boolean serverLagging = false;
    
    public LagNotifier() {
        super(100, 100, 10, 10);
    }
    
    public void draw() {
        if(serverLagging && hudDraw.getValue()) {
            String str = "Server is not responding! (" + Integer.toString(currentTick - lastPacketTick) + ")";
            drawString(str, getX(), getY(), new Color(Integer.parseInt("FFFFFF", 16)));
            
            setWidth((int) FONT_RENDERER.getStringWidth(str));
            setHeight(FONT_RENDERER.getFontHeight());
        }
    }
    
    @Override
    public void onTick() {
        currentTick++;
        if(currentTick - lastPacketTick >= maxTicks.getValue() && !serverLagging) {
            serverLagging = true;
            if(chatMessage.getValue()) UTILS.printMessage("Server is not responding!");
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
