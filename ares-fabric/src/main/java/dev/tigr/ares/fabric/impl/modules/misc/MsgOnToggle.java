package dev.tigr.ares.fabric.impl.modules.misc;

import dev.tigr.ares.core.event.client.ToggleEvent;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import dev.tigr.simpleevents.listener.Priority;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "MsgOnToggle", description = "Sends a chat message when a module is toggled", category = Category.MISC)
public class MsgOnToggle extends Module {
    public static MsgOnToggle INSTANCE;

    public MsgOnToggle() {
        INSTANCE = this;
    }

    @EventHandler
    public EventListener<ToggleEvent> toggleEvent = new EventListener<>(event -> {
        if(event.getModule().getName().equalsIgnoreCase("clickgui")) return;
        String text = event.isEnabled() ? TextColor.GREEN + "Enabled " : TextColor.RED + "Disabled ";
        UTILS.printMessage(text + TextColor.BLUE + event.getModule().getName());
    });
}