package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.lwjgl.opengl.GL11;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Chams", description = "Render entities though walls", category = Category.RENDER)
public class Chams extends Module {
    public final Setting<Boolean> players = register(new BooleanSetting("Players", true));
    public final Setting<Boolean> entities = register(new BooleanSetting("Entities", true));

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
        return entity instanceof EntityPlayer && players.getValue() || entities.getValue();
    }
}
