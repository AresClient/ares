package dev.tigr.ares.fabric.impl.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.event.render.RenderNametagsEvent;
import dev.tigr.ares.fabric.impl.render.CustomRenderStack;
import dev.tigr.ares.fabric.mixin.accessors.MatrixStackAccessor;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Deque;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "NameTags", description = "Replace vanilla nametags with better ones", category = Category.RENDER)
public class NameTags extends Module {
    private static final Color BACKGROUND_COLOR = new Color(0.0F, 0.0F, 0.0F, 0.5F);

    private final Setting<Double> scale = register(new DoubleSetting("Scale", 4, 2, 5));
    private final Setting<Double> max = register(new DoubleSetting("Max", 2, 0, 5));
    private final Color shadow = new Color(-1);
    private final Color color = Color.WHITE;

    @EventHandler
    private EventListener<RenderNametagsEvent> renderNametagsEvent = new EventListener<>(event -> {
        event.setCancelled(true);
        renderNametag(event.getPlayerEntity(), event.getPlayerEntity().getEntityName(), event.getMatrixStack());
    });

    protected void renderNametag(AbstractClientPlayerEntity abstractClientPlayerEntity, String text, MatrixStack matrixStack) {
        float f = abstractClientPlayerEntity.getHeight() + 0.5F;
        int verticalShift = "deadmau5".equals(text) ? -10 : 0;
        float fScale = (float) Math.max(Math.min(MC.player.distanceTo(abstractClientPlayerEntity) / (100 * scale.getValue()), max.getValue()/50), 1/80d);

        // push matrix from nametag event to render stack
        Deque<MatrixStack.Entry> stack = ((MatrixStackAccessor) ((CustomRenderStack)RENDER_STACK).getMatrixStack()).getStack();
        stack.addLast(matrixStack.peek());

        RENDER_STACK.translate(0, f, 0);
        ((CustomRenderStack)RENDER_STACK).getMatrixStack().multiply(MC.getEntityRenderDispatcher().getRotation());
        RENDER_STACK.scale(-fScale, -fScale, fScale);
        RenderSystem.disableDepthTest();

        // calculate health
        int health = (int) (abstractClientPlayerEntity.getHealth() + MC.player.getAbsorptionAmount());
        String healthColor = "FFFFFF";
        if(health >= 15) healthColor = "00FF00";
        else if(health > 10) healthColor = "FFF000";
        else if(health < 10) healthColor = "FF0000";
        String sHealth = " " + health;

        double i = (FONT_RENDERER.getStringWidth(text) / 2) + (FONT_RENDERER.getStringWidth(sHealth) / 2);
        RENDERER.drawRect(-i - 1, 8 + verticalShift, 2*i + 2, -9 - (FONT_RENDERER.getFontHeight() - 9), BACKGROUND_COLOR);

        // render name
        FONT_RENDERER.drawString(text, -i, verticalShift - (FONT_RENDERER.getFontHeight() - 9) - 1, color);
        FONT_RENDERER.drawString(text, -i, verticalShift - (FONT_RENDERER.getFontHeight() - 9) - 1, shadow);
        FONT_RENDERER.drawString(sHealth, -i + FONT_RENDERER.getStringWidth(text), verticalShift - (FONT_RENDERER.getFontHeight() - 9) - 1, new Color(Integer.parseInt(healthColor, 16)));

        RENDER_STACK.pop();
    }
}
