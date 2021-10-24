package dev.tigr.ares.forge.mixin.render;

import com.google.common.base.MoreObjects;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.forge.event.events.render.RenderHeldItemEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer implements Wrapper {
    @Shadow private float prevEquippedProgressMainHand;

    @Shadow private float equippedProgressMainHand;

    @Shadow public abstract void renderItemInFirstPerson(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_);

    @Shadow private ItemStack itemStackMainHand;

    @Shadow private float prevEquippedProgressOffHand;

    @Shadow private float equippedProgressOffHand;

    @Shadow private ItemStack itemStackOffHand;

    @Shadow protected abstract void rotateArroundXAndY(float angle, float angleY);

    @Shadow protected abstract void setLightmap();

    @Shadow protected abstract void rotateArm(float p_187458_1_);

    @Inject(method = "renderItemInFirstPerson(F)V", at = @At("HEAD"), cancellable = true)
    public void onRenderItem(float partialTicks, CallbackInfo ci) {
        if(!Ares.EVENT_MANAGER.post(new RenderHeldItemEvent.Invoke()).isCancelled()) return;

        ci.cancel();

        AbstractClientPlayer abstractclientplayer = MC.player;
        float f = abstractclientplayer.getSwingProgress(partialTicks);
        EnumHand enumhand = MoreObjects.firstNonNull(abstractclientplayer.swingingHand, EnumHand.MAIN_HAND);
        float f1 = abstractclientplayer.prevRotationPitch + (abstractclientplayer.rotationPitch - abstractclientplayer.prevRotationPitch) * partialTicks;
        float f2 = abstractclientplayer.prevRotationYaw + (abstractclientplayer.rotationYaw - abstractclientplayer.prevRotationYaw) * partialTicks;
        boolean flag = true;
        boolean flag1 = true;

        if(abstractclientplayer.isHandActive()) {
            ItemStack itemstack = abstractclientplayer.getActiveItemStack();

            if(itemstack.getItem() instanceof net.minecraft.item.ItemBow) {
                EnumHand enumhand1 = abstractclientplayer.getActiveHand();
                flag = enumhand1 == EnumHand.MAIN_HAND;
                flag1 = !flag;
            }
        }

        rotateArroundXAndY(f1, f2);
        setLightmap();
        rotateArm(partialTicks);
        GlStateManager.enableRescaleNormal();

        if(flag) {
            GlStateManager.pushMatrix(); // Save a backup of current matrices

            RenderHeldItemEvent.Cancelled e = Ares.EVENT_MANAGER.post(new RenderHeldItemEvent.Cancelled(EnumHand.MAIN_HAND));

            GlStateManager.translate(e.getTranslation().x, e.getTranslation().y, e.getTranslation().z);
            GlStateManager.scale(e.getScale().x, e.getScale().y, e.getScale().z);
            GlStateManager.rotate(e.getRotation());

            float f3 = enumhand == EnumHand.MAIN_HAND ? f : 0.0F;
            float f5 = 1.0F - (prevEquippedProgressMainHand + (equippedProgressMainHand - prevEquippedProgressMainHand) * partialTicks);
            if(!net.minecraftforge.client.ForgeHooksClient.renderSpecificFirstPersonHand(EnumHand.MAIN_HAND, partialTicks, f1, f3, f5, itemStackMainHand))
                renderItemInFirstPerson(abstractclientplayer, partialTicks, f1, EnumHand.MAIN_HAND, f3, itemStackMainHand, f5);

            GlStateManager.popMatrix(); // Use saved backup to get rid of transformations from main hand
        }

        if(flag1) {
            GlStateManager.pushMatrix();

            RenderHeldItemEvent.Cancelled e = Ares.EVENT_MANAGER.post(new RenderHeldItemEvent.Cancelled(EnumHand.OFF_HAND));

            GlStateManager.translate(e.getTranslation().x, e.getTranslation().y, e.getTranslation().z);
            GlStateManager.scale(e.getScale().x, e.getScale().y, e.getScale().z);
            GlStateManager.rotate(e.getRotation());

            float f4 = enumhand == EnumHand.OFF_HAND ? f : 0.0F;
            float f6 = 1.0F - (prevEquippedProgressOffHand + (equippedProgressOffHand - prevEquippedProgressOffHand) * partialTicks);
            if(!net.minecraftforge.client.ForgeHooksClient.renderSpecificFirstPersonHand(EnumHand.OFF_HAND, partialTicks, f1, f4, f6, itemStackOffHand))
                renderItemInFirstPerson(abstractclientplayer, partialTicks, f1, EnumHand.OFF_HAND, f4, itemStackOffHand, f6);

            GlStateManager.popMatrix();
        }

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }
}
