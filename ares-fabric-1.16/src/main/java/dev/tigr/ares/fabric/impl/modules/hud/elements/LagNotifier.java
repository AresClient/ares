package dev.tigr.ares.fabric.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.impl.modules.hud.HudElement;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.gui.screen.Screen;

/**
 * @author nwroot
 */
@Module.Info(name = "LagNotifier", description = "Notifies if the server stopped sending packets", category = Category.HUD)
public class LagNotifier extends HudElement {
    private final Setting<Integer> maxTicks = register(new IntegerSetting("Max Ticks", 40, 10, 100));
    private final Setting<Boolean> hudDraw = register(new BooleanSetting("Draw Overlay", true));
    private final Setting<Boolean> chatMessage = register(new BooleanSetting("Chat Message", false));
    private final Setting<Boolean> reconnectMessage = register(new BooleanSetting("Reconnect Message", true));
    
    private int currentTick = 0;
    private int lastPacketTick = 0;
    private boolean serverLagging = false;
    
    public LagNotifier() {
        super(0, 0, 0, 0);
        background.setVisibility(() -> false);
    }
    
    public void draw() {
        if(serverLagging && hudDraw.getValue()) {
            String str = TextColor.RED + "Server is not responding! (" + (currentTick - lastPacketTick) + ")";
            drawString(str, ((double) MC.getWindow().getScaledWidth() / 2d) - (FONT_RENDERER.getStringWidth(str) / 2d), 5, Color.RED);
            
            setWidth((int) FONT_RENDERER.getStringWidth(str));
            setHeight(FONT_RENDERER.getFontHeight());
        }
    }
    
    @Override
    public void onTick() {
        currentTick++;
        if(currentTick - lastPacketTick >= maxTicks.getValue() && !serverLagging) {
            serverLagging = true;
            if(chatMessage.getValue()) UTILS.printMessage(TextColor.RED + "Server is not responding!");
        }
    }
    
    @EventHandler
    public EventListener<PacketEvent.Receive> packetReceiveEvent = new EventListener<>(event -> {
        if(serverLagging && reconnectMessage.getValue()) {
            UTILS.printMessage(TextColor.RED + "Received a server packet after " + (currentTick - lastPacketTick) + " client ticks");
        }
        lastPacketTick = currentTick;
        serverLagging = false;
    });

    @Override
    protected void onClick(double mouseX, double mouseY, int mouseButton) {
    }

    @Override
    protected void onEditDraw(double mouseX, double mouseY, Screen screen) {
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }
}
