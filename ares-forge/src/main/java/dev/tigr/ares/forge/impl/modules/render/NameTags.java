package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IFontRenderer;
import dev.tigr.ares.forge.event.events.render.RenderNametagsEvent;
import dev.tigr.ares.forge.utils.RenderUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "NameTags", description = "Replace vanilla nametags with better ones", category = Category.RENDER)
public class NameTags extends Module {
    private final Setting<Double> scale = register(new DoubleSetting("Scale", 4, 2, 5));
    private final Setting<Double> max = register(new DoubleSetting("Max", 2, 0, 5));
    private final Color shadow = new Color(-1);
    private final Color color = Color.WHITE;

    @EventHandler
    public EventListener<RenderNametagsEvent> renderVanillaNametagsEvent = new EventListener<>(event -> event.setCancelled(true));

    @Override
    public void onRender3d() {
        MC.world.playerEntities.stream().filter(entityPlayer -> entityPlayer != MC.player).forEach(entityPlayer -> {
            Vec3d pos = RenderUtils.getRenderPos(entityPlayer);
            renderNametag(entityPlayer, pos.x, pos.y, pos.z, FONT_RENDERER);
        });
    }

    private void renderNametag(EntityPlayer player, double x, double y, double z, IFontRenderer fontRendererIn) {
        String name = player.getDisplayName().getUnformattedText();
        GlStateManager.alphaFunc(516, 0.1F);

        float viewerYaw = MC.getRenderManager().playerViewY;
        float viewerPitch = MC.getRenderManager().playerViewX;
        boolean isThirdPersonFrontal = MC.getRenderManager().options.thirdPersonView == 2;
        float f2 = player.height + 0.5F - (player.isSneaking() ? 0.25F : 0.0F);
        int verticalShift = "deadmau5".equals(name) ? -10 : 0;
        double fScale = Math.max(Math.min(MC.player.getDistance(player) / (100 * scale.getValue()), max.getValue()/50), 1/80d);

        y = y + f2;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-fScale, -fScale, -fScale);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();

        // calculate health
        int health = (int) (player.getHealth() + MC.player.getAbsorptionAmount());
        String healthColor = "FFFFFF";
        if(health >= 15) healthColor = "00FF00";
        else if(health > 10) healthColor = "FFF000";
        else if(health < 10) healthColor = "FF0000";
        String sHealth = " " + health;

        // background
        double i = (fontRendererIn.getStringWidth(name) / 2) + (fontRendererIn.getStringWidth(sHealth) / 2);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((-i - 1), (-1 - (fontRendererIn.getFontHeight() - 9) + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.pos((-i - 1), (8 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.pos((i + 1), (8 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.pos((i + 1), (-1 - (fontRendererIn.getFontHeight() - 9) + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();

        // render name + health
        fontRendererIn.drawString(name, -i, verticalShift - (fontRendererIn.getFontHeight() - 9) - 1, color);
        fontRendererIn.drawString(name, -i, verticalShift - (fontRendererIn.getFontHeight() - 9) - 1, shadow);
        fontRendererIn.drawString(sHealth, -i + fontRendererIn.getStringWidth(name), verticalShift - (fontRendererIn.getFontHeight() - 9) - 1, new Color(Integer.parseInt(healthColor, 16)));

        RenderHelper.enableStandardItemLighting();
        GlStateManager.scale(1.0f, 1.0f, 0.01f);

        // render armor
        int armorX = -37;
        int armorY = (-1 + verticalShift) - 17;
        for(int item = 3; item >= 0; item--) {
            MC.getRenderItem().renderItemAndEffectIntoGUI(player.inventory.armorInventory.get(item), armorX, armorY);
            MC.getRenderItem().renderItemOverlays(MC.fontRenderer, player.inventory.armorInventory.get(item), armorX, armorY);
            GlStateManager.disableDepth();
            armorX += 17 + 2;
        }

        // render items in hand
        MC.getRenderItem().renderItemAndEffectIntoGUI(player.getHeldItemMainhand(), armorX - 95, armorY);
        MC.getRenderItem().renderItemOverlays(MC.fontRenderer, player.getHeldItemMainhand(), armorX - 95, armorY);
        GlStateManager.disableDepth();

        MC.getRenderItem().renderItemAndEffectIntoGUI(player.getHeldItemOffhand(), armorX, armorY);
        MC.getRenderItem().renderItemOverlays(MC.fontRenderer, player.getHeldItemOffhand(), armorX, armorY);
        GlStateManager.disableDepth();

        GlStateManager.scale(1.0f, 1.0f, 1.0f);
        RenderHelper.disableStandardItemLighting();

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
