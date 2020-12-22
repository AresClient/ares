package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.lwjgl.opengl.GL11;

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
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0F, -1000000);
        }
    });

    @EventHandler
    public EventListener<RenderLivingEvent.Post> livingRenderPostEvent = new EventListener<>(event -> {
        if(shouldRender(event.getEntity())) {
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
            GlStateManager.doPolygonOffset(1.0F, 1000000);
            GlStateManager.disablePolygonOffset();
        }
    });

    private boolean shouldRender(EntityLivingBase entity) {
        return WorldUtils.isTarget(entity, players.getValue(), friends.getValue(), teammates.getValue(), passive.getValue(), hostile.getValue(), nametagged.getValue(), bots.getValue());
    }
}
