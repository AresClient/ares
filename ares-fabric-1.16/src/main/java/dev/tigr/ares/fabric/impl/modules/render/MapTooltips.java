package dev.tigr.ares.fabric.impl.modules.render;


import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.render.ItemTooltipEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.*;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tigermouthbear 12/11/20
 */
@Module.Info(name = "MapTooltips", description = "Shows a preview of maps as a tooltip", category = Category.RENDER)
public class MapTooltips extends Module {
    private static final Identifier MAP_BACKGROUND = new Identifier("textures/map/map_background.png");
    private static final Identifier MAP_BACKGROUND_CHECKERBOARD = new Identifier("textures/map/map_background_checkerboard.png");

    // super smart way to get MapTexture class ;)
    private static final Class<?> MAP_TEXTURE_CLASS = Arrays.stream(MapRenderer.class.getDeclaredClasses())
            .filter(clazz -> clazz.getSimpleName().equals("MapTexture")
                    || clazz.getSimpleName().equals("class_331")).findAny().orElse(null);

    private static final Field RENDER_LAYER_FIELD = Arrays.stream(MAP_TEXTURE_CLASS.getDeclaredFields())
            .filter(field -> field.getName().equals("renderLayer")
                    || field.getName().equals("field_21689")).findAny().orElse(null);

    @EventHandler
    public EventListener<ItemTooltipEvent.Post> renderTooltipPostTextEvent = new EventListener<>(event -> {
        if(!event.getItemStack().isEmpty() && event.getItemStack().getItem() instanceof FilledMapItem && MAP_TEXTURE_CLASS != null) {
            // find tooltip height
            List<Text> textList = event.getItemStack().getTooltip(MC.player, MC.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL);
            int height = 8;
            if(textList.size() > 1) {
                height += 2 + (textList.size() - 1) * 10;
            }

            // get map
            MapState mapState = FilledMapItem.getOrCreateMapState(event.getItemStack(), MC.world);

            // start render
            RenderSystem.pushMatrix();
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableAlphaTest();
            RenderSystem.disableLighting();

            // move to pos
            RenderSystem.translated(event.getX() + 17, event.getY() + height, 201);
            RenderSystem.scaled(0.5F, 0.5F, 1);

            // draw background
            MC.getTextureManager().bindTexture(mapState == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
            bufferbuilder.vertex(-7.0D, 135.0D, 0.0D).texture(0, 1).next();
            bufferbuilder.vertex(135.0D, 135.0D, 0.0D).texture(1, 1).next();
            bufferbuilder.vertex(135.0D, -7.0D, 0.0D).texture(1, 0).next();
            bufferbuilder.vertex(-7.0D, -7.0D, 0.0D).texture(0, 0).next();
            tessellator.draw();

            // draw map if not null
            if(mapState != null) {
                VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                RenderLayer renderLayer;
                try {
                    renderLayer = (RenderLayer) RENDER_LAYER_FIELD.get(MC.gameRenderer.getMapRenderer().getTexture(mapState.getId()));
                } catch(IllegalAccessException e) {
                    return;
                }
                VertexConsumer vertexConsumer = immediate.getBuffer(renderLayer);
                vertexConsumer.vertex(0.0F, 128.0F, 401F).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(15728880).next();
                vertexConsumer.vertex(128.0F, 128.0F, 401F).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(15728880).next();
                vertexConsumer.vertex(128.0F, 0.0F, 401F).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(15728880).next();
                vertexConsumer.vertex(0.0F, 0.0F, 401F).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(15728880).next();
                immediate.draw();
            }

            // cleanup
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
            RenderSystem.enableTexture();
            RenderSystem.popMatrix();
        }
    });
}
