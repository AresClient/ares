package dev.tigr.ares.forge.utils;

import com.google.common.base.MoreObjects;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.render.RenderHeldItemEvent;
import dev.tigr.ares.forge.mixin.accessor.ItemRendererAccessor;
import dev.tigr.ares.forge.mixininterface.IItemRenderer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

/**
 * This just holds methods taken from minecraft which had to be modified and are used with mixins
 */
public class Reimplementations implements Wrapper {
    /**
     * net.minecraft.client.renderer.ItemRenderer#renderItemInFirstPerson(float)
     * MixinEntityRenderer
     */
    public static void renderItemInFirstPerson(ItemRenderer itemRenderer, float partialTicks) {
        AbstractClientPlayer abstractclientplayer = MC.player;
        float f = abstractclientplayer.getSwingProgress(partialTicks);
        EnumHand enumhand = MoreObjects.firstNonNull(abstractclientplayer.swingingHand, EnumHand.MAIN_HAND);
        float f1 = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
        float f2 = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
        boolean flag = true;
        boolean flag1 = true;

        if (abstractclientplayer.isHandActive()) {
            ItemStack itemstack = abstractclientplayer.getActiveItemStack();

            if (itemstack.getItem() instanceof net.minecraft.item.ItemBow) {
                EnumHand enumhand1 = abstractclientplayer.getActiveHand();
                flag = enumhand1 == EnumHand.MAIN_HAND;
                flag1 = !flag;
            }
        }

        ((IItemRenderer)itemRenderer).doRotateArroundXAndY(f1, f2);
        ((IItemRenderer)itemRenderer).doSetLightMap();
        ((IItemRenderer)itemRenderer).doRotateArm(partialTicks);
        GlStateManager.enableRescaleNormal();

        if (flag) {
            GlStateManager.pushMatrix(); // Save a backup of current matrices

            RenderHeldItemEvent.Cancelled e = Ares.EVENT_MANAGER.post(new RenderHeldItemEvent.Cancelled(EnumHand.MAIN_HAND));

            GlStateManager.translate(e.getTranslation().x, e.getTranslation().y, e.getTranslation().z);
            GlStateManager.scale(e.getScale().x, e.getScale().y, e.getScale().z);
            GlStateManager.rotate(e.getRotation());

            float f3 = enumhand == EnumHand.MAIN_HAND ? f : 0.0F;
            float f5 = 1.0F - (((ItemRendererAccessor)itemRenderer).getPrevEquippedProgressMainHand() + (((ItemRendererAccessor)itemRenderer).getEquippedProgressMainHand() - ((ItemRendererAccessor)itemRenderer).getPrevEquippedProgressMainHand()) * partialTicks);
            if(!net.minecraftforge.client.ForgeHooksClient.renderSpecificFirstPersonHand(EnumHand.MAIN_HAND, partialTicks, f1, f3, f5, ((ItemRendererAccessor)itemRenderer).getItemStackMainHand()))
                itemRenderer.renderItemInFirstPerson(abstractclientplayer, partialTicks, f1, EnumHand.MAIN_HAND, f3, ((ItemRendererAccessor)itemRenderer).getItemStackMainHand(), f5);

            GlStateManager.popMatrix(); // Use saved backup to get rid of transformations from main hand
        }

        if (flag1) {
            GlStateManager.pushMatrix();

            RenderHeldItemEvent.Cancelled e = Ares.EVENT_MANAGER.post(new RenderHeldItemEvent.Cancelled(EnumHand.OFF_HAND));

            GlStateManager.translate(e.getTranslation().x, e.getTranslation().y, e.getTranslation().z);
            GlStateManager.scale(e.getScale().x, e.getScale().y, e.getScale().z);
            GlStateManager.rotate(e.getRotation());

            float f4 = enumhand == EnumHand.OFF_HAND ? f : 0.0F;
            float f6 = 1.0F - (((ItemRendererAccessor)itemRenderer).getPrevEquippedProgressOffHand() + (((ItemRendererAccessor)itemRenderer).getEquippedProgressOffHand() - ((ItemRendererAccessor)itemRenderer).getPrevEquippedProgressOffHand()) * partialTicks);
            if(!net.minecraftforge.client.ForgeHooksClient.renderSpecificFirstPersonHand(EnumHand.OFF_HAND, partialTicks, f1, f4, f6, ((ItemRendererAccessor)itemRenderer).getItemStackOffHand()))
                itemRenderer.renderItemInFirstPerson(abstractclientplayer, partialTicks, f1, EnumHand.OFF_HAND, f4, ((ItemRendererAccessor)itemRenderer).getItemStackOffHand(), f6);

            GlStateManager.popMatrix();
        }

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }
}
