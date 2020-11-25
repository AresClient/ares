package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.forge.event.events.player.ExtraTabEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ExtraTab", description = "Sets the max players to render in the player tab higher or lower", category = Category.RENDER)
public class ExtraTab extends Module {
    private final Setting<Integer> size = register(new IntegerSetting("Size", 300, 0, 500));

    @EventHandler
    public EventListener<ExtraTabEvent> extraTabEvent = new EventListener<>(event -> event.setNum(size.getValue()));
}
