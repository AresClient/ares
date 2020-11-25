package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.event.render.CameraClipEvent;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.simpleevents.event.Result;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "CameraClip", description = "Allows the 3rd person camera to go through walls", category = Category.RENDER)
public class CameraClip extends Module {
    @EventHandler
    public EventListener<CameraClipEvent> cameraClipEvent = new EventListener<>(event -> event.setResult(Result.ALLOW));
}
