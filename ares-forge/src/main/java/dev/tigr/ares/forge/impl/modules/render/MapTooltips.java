package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.RenderTooltipEvent;
import org.lwjgl.opengl.GL11;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "MapTooltips", description = "Shows a preview of maps as a tooltip", category = Category.RENDER)
public class MapTooltips extends Module {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    private static final float SIZE = 135;
    private static final float SCALE = 0.5F;

    @EventHandler
    public EventListener<RenderTooltipEvent.PostText> renderTooltipPostTextEvent = new EventListener<>(event -> {
        if(!event.getStack().isEmpty() && event.getStack().getItem() instanceof ItemMap) {
            MapData mapData = ((ItemMap) event.getStack().getItem()).getMapData(event.getStack(), MC.world);

            if(mapData != null) {
                GlStateManager.pushMatrix();

                GlStateManager.color(1, 1, 1, 1);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.translate(event.getX(), event.getY() - SIZE * SCALE - 5, 0);
                GlStateManager.scale(SCALE, SCALE, SCALE);

                // draw background
                MC.getTextureManager().bindTexture(BACKGROUND);
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
                bufferbuilder.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
                bufferbuilder.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
                bufferbuilder.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
                tessellator.draw();

                // draw map
                MC.entityRenderer.getMapItemRenderer().renderMap(mapData, false);

                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }
        }
    });
}
