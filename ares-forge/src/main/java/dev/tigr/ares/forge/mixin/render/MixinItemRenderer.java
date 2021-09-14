package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.forge.mixininterface.IItemRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer implements IItemRenderer {
    @Shadow
    protected abstract void rotateArroundXAndY(float angle, float angleY);

    @Shadow
    protected abstract void setLightmap();

    @Shadow
    protected abstract void rotateArm(float p_187458_1_);

    @Override
    public void doRotateArroundXAndY(float angle, float angleY) {
        rotateArroundXAndY(angle, angleY);
    }

    @Override
    public void doSetLightMap() {
        setLightmap();
    }

    @Override
    public void doRotateArm(float p_187458_1_) {
        rotateArm(p_187458_1_);
    }
}
