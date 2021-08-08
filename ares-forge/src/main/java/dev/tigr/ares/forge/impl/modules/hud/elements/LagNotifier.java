package dev.tigr.ares.forge.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.ares.forge.impl.modules.hud.HudElement;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.gui.GuiScreen;

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
            drawString(str, (double) (MC.displayWidth / 2) - (FONT_RENDERER.getStringWidth(str) / 2d), 5, Color.RED);
            
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
    protected void onEditDraw(int mouseX, int mouseY, GuiScreen screen) {
    }

    @Override
    protected void onClick(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return false;
    }
}
