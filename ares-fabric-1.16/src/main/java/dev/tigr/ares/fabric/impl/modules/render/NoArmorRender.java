package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.fabric.event.render.ArmorRenderEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "NoArmorRender", description = "Prevents armor from being rendered", category = Category.RENDER)
public class NoArmorRender extends Module {
    private final Setting<Boolean> hat = register(new BooleanSetting("Hat", true));
    private final Setting<Boolean> shirt = register(new BooleanSetting("Shirt", true));
    private final Setting<Boolean> pants = register(new BooleanSetting("Pants", true));
    private final Setting<Boolean> shoes = register(new BooleanSetting("Shoes", true));

    @EventHandler
    public EventListener<ArmorRenderEvent> armorRenderEvent = new EventListener<>(event -> {
        event.setHat(!hat.getValue());
        event.setShirt(!shirt.getValue());
        event.setPants(!pants.getValue());
        event.setShoes(!shoes.getValue());
    });
}
