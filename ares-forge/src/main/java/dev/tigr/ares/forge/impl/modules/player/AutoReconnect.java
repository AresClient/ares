package dev.tigr.ares.forge.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import dev.tigr.simpleevents.listener.Priority;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.GuiOpenEvent;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoReconnect", description = "Automatically reconnect at a specific interval", category = Category.PLAYER, alwaysListening = true)
public class AutoReconnect extends Module {
    private final Setting<Double> delay = register(new DoubleSetting("Delay", 1, 0, 5));
    @EventHandler
    public EventListener<GuiOpenEvent> openGuiEvent = new EventListener<>(Priority.HIGHEST, event -> {
        if(getEnabled() && event.getGui() instanceof GuiDisconnected)
            event.setGui(new Gui((GuiDisconnected) event.getGui()));
    });
    private ServerData serverData = null;

    @Override
    public void onTick() {
        serverData = MC.getCurrentServerData() != null ? MC.getCurrentServerData() : serverData;
    }

    public class Gui extends GuiDisconnected {
        private final long endTime;

        public Gui(GuiDisconnected guiDisconnected) {
            super(
                    new GuiMultiplayer(new GuiMainMenu()),
                    ReflectionHelper.getPrivateValue(GuiDisconnected.class, guiDisconnected, "reason", "field_146306_a"),
                    ReflectionHelper.getPrivateValue(GuiDisconnected.class, guiDisconnected, "message", "field_146304_f")
            );

            endTime = (long) (System.currentTimeMillis() + (delay.getValue() * 1000));
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);

            long time = endTime - System.currentTimeMillis();
            if(time <= 0) MC.displayGuiScreen(new GuiConnecting(this, MC, serverData));

            String text = "Reconnecting in " + time + "ms";
            fontRenderer.drawStringWithShadow(text, width / 2 - fontRenderer.getStringWidth(text) / 2, 2, Color.WHITE.getRGB());
        }
    }
}
