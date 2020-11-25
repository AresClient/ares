package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.fabric.event.render.RenderLivingEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.opengl.GL11;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Chams", description = "Render entities though walls", category = Category.RENDER)
public class Chams extends Module {
    public Setting<Boolean> players = register(new BooleanSetting("Players", true));
    public Setting<Boolean> entities = register(new BooleanSetting("Entities", true));

    @EventHandler
    public EventListener<RenderLivingEvent.Pre> livingRenderPreEvent = new EventListener<>(event -> {
        if(shouldRender(event.getEntity())) {
            GL11.glEnable(32823);
            GL11.glPolygonOffset(1.0f, -1000000.0f);
        }
    });

    @EventHandler
    public EventListener<RenderLivingEvent.Post> livingRenderPostEvent = new EventListener<>(event -> {
        if(shouldRender(event.getEntity())) {
            GL11.glPolygonOffset(1.0f, 1000000.0f);
            GL11.glDisable(32823);
        }
    });

    private boolean shouldRender(Entity entity) {
        return entity instanceof PlayerEntity && players.getValue() || entities.getValue();
    }
}
