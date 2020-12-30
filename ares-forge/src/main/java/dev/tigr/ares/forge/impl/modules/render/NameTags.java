package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.forge.event.events.render.RenderNametagsEvent;
import dev.tigr.ares.forge.utils.RenderUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "NameTags", description = "Replace vanilla nametags with better ones", category = Category.RENDER)
public class NameTags extends Module {
    private static final Color BACKGROUND_COLOR = new Color(0.0F, 0.0F, 0.0F, 0.5F);
    private static final Color SHADOW = new Color(-1);

    private final Setting<Double> scale = register(new DoubleSetting("Scale", 4, 2, 5));
    private final Setting<Double> max = register(new DoubleSetting("Max", 2, 0, 5));

    @EventHandler
    public EventListener<RenderNametagsEvent> renderVanillaNametagsEvent = new EventListener<>(event -> event.setCancelled(true));

    @Override
    public void onRender3d() {
        MC.world.playerEntities.stream().filter(entityPlayer -> entityPlayer != MC.player).forEach(entityPlayer -> {
            Vec3d pos = RenderUtils.getRenderPos(entityPlayer);
            renderNametag(entityPlayer, pos.x, pos.y, pos.z);
        });
    }

    private void renderNametag(EntityPlayer player, double x, double y, double z) {
        String name = player.getDisplayName().getUnformattedText();
        GlStateManager.alphaFunc(516, 0.1F);

        float viewerYaw = MC.getRenderManager().playerViewY;
        float viewerPitch = MC.getRenderManager().playerViewX;
        boolean isThirdPersonFrontal = MC.getRenderManager().options.thirdPersonView == 2;
        y += player.height + 0.5F - (player.isSneaking() ? 0.25F : 0.0F);
        int verticalShift = "deadmau5".equals(name) ? -10 : 0;
        double fScale = Math.max(Math.min(MC.player.getDistance(player) / (100 * scale.getValue()), max.getValue()/50), 1/80d);

        RENDER_STACK.push();
        RENDER_STACK.translate(x, y, z);
        RENDER_STACK.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        RENDER_STACK.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
        RENDER_STACK.scale(-fScale, -fScale, -fScale);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        // calculate health
        int health = (int) (player.getHealth() + player.getAbsorptionAmount());
        String healthColor = "FFFFFF";
        if(health >= 15) healthColor = "00FF00";
        else if(health > 10) healthColor = "FFF000";
        else if(health < 10) healthColor = "FF0000";
        String sHealth = " " + health;

        // background
        double i = (FONT_RENDERER.getStringWidth(name) / 2) + (FONT_RENDERER.getStringWidth(sHealth) / 2);
        RENDERER.drawRect(-i - 1, 8 + verticalShift, 2*i + 2, -9 - (FONT_RENDERER.getFontHeight() - 9), BACKGROUND_COLOR);

        // render name + health
        FONT_RENDERER.drawString(name, -i, verticalShift - (FONT_RENDERER.getFontHeight() - 9) - 1, Color.WHITE);
        FONT_RENDERER.drawString(name, -i, verticalShift - (FONT_RENDERER.getFontHeight() - 9) - 1, SHADOW);
        FONT_RENDERER.drawString(sHealth, -i + FONT_RENDERER.getStringWidth(name), verticalShift - (FONT_RENDERER.getFontHeight() - 9) - 1, new Color(Integer.parseInt(healthColor, 16)));

        // render items
        RENDER_STACK.scale(1.0f, 1.0f, 0.01f);
        // render armor
        int armorX = -37;
        int armorY = (-1 + verticalShift) - 17;
        for(int item = 3; item >= 0; item--) {
            RenderUtils.renderItem(player.inventory.armorInventory.get(item), armorX, armorY);
            armorX += 17 + 2;
        }

        // render items in hand
        RenderUtils.renderItem(player.getHeldItemMainhand(), armorX - 95, armorY);
        RenderUtils.renderItem(player.getHeldItemOffhand(), armorX, armorY);
        RENDER_STACK.scale(1.0f, 1.0f, 1.0f);

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        RENDER_STACK.pop();
    }
}
