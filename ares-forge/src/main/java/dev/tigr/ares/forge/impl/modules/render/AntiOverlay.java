package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AntiOverlay", description = "Prevents some render overlays", category = Category.RENDER)
public class AntiOverlay extends Module {
    private final Setting<Boolean> allowFire = register(new BooleanSetting("Fire", true));
    private final Setting<Boolean> allowBlocks = register(new BooleanSetting("Blocks", true));
    private final Setting<Boolean> allowWater = register(new BooleanSetting("Water", true));

    @EventHandler
    public EventListener<RenderBlockOverlayEvent> blockOverlayEvent = new EventListener<>(event -> {
        boolean shouldRender = false;

        if(event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.FIRE) {
            if(allowFire.getValue()) shouldRender = true;
        } else if(event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.BLOCK) {
            if(allowBlocks.getValue()) shouldRender = true;
        } else if(event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.WATER) {
            if(allowWater.getValue()) shouldRender = true;
        }

        event.setCanceled(shouldRender);
    });
}
