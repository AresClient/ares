package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.fabric.event.render.RenderLivingEvent;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL20;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Chams", description = "Render entities though walls", category = Category.RENDER)
public class Chams extends Module {
    private final Setting<Boolean> players = register(new BooleanSetting("Players", true));
    private final Setting<Boolean> friends = register(new BooleanSetting("Friends", true)).setVisibility(players::getValue);
    private final Setting<Boolean> teammates = register(new BooleanSetting("Teammates", true)).setVisibility(players::getValue);
    private final Setting<Boolean> passive = register(new BooleanSetting("Passive", true));
    private final Setting<Boolean> hostile = register(new BooleanSetting("Hostile", true));
    private final Setting<Boolean> nametagged = register(new BooleanSetting("Nametagged", true));
    private final Setting<Boolean> bots = register(new BooleanSetting("Bots", false));
    @EventHandler
    public EventListener<RenderLivingEvent.Pre> livingRenderPreEvent = new EventListener<>(event -> {
        if(shouldRender(event.getEntity())) {
            GL20.glEnable(32823);
            GL20.glPolygonOffset(1.0f, -1000000.0f);
        }
    });

    @EventHandler
    public EventListener<RenderLivingEvent.Post> livingRenderPostEvent = new EventListener<>(event -> {
        if(shouldRender(event.getEntity())) {
            GL20.glPolygonOffset(1.0f, 1000000.0f);
            GL20.glDisable(32823);
        }
    });

    private boolean shouldRender(Entity entity) {
        return WorldUtils.isTarget(entity, players.getValue(), friends.getValue(), teammates.getValue(), passive.getValue(), hostile.getValue(), nametagged.getValue(), bots.getValue());
    }
}
