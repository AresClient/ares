package dev.tigr.ares.fabric.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.fabric.event.client.ToggleEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

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
    public EventListener<ToggleEvent.Enabled> enabledEvent = new EventListener<>(event -> {
        if(event.getModule().getName().equalsIgnoreCase("clickgui")) return;
        UTILS.printMessage(TextColor.GREEN + "Enabled " + TextColor.BLUE + event.getModule().getName());
    });
    @EventHandler
    public EventListener<ToggleEvent.Disabled> disabledEvent = new EventListener<>(event -> {
        if(event.getModule().getName().equalsIgnoreCase("clickgui")) return;
        UTILS.printMessage(TextColor.RED + "Disabled " + TextColor.BLUE + event.getModule().getName());
    });
}