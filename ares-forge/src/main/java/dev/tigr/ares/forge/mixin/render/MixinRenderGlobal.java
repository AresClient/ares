package dev.tigr.ares.forge.mixin.render;

import dev.tigr.ares.core.feature.module.modules.player.Freecam;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static dev.tigr.ares.Wrapper.MC;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
    @ModifyVariable(method = "setupTerrain", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    public BlockPos setupTerrainStoreFlooredChunkPosition(BlockPos playerPos) {
        if(Freecam.INSTANCE.getEnabled())
            playerPos = new BlockPos(MathHelper.floor(MC.player.posX / 16.0D) * 16, MathHelper.floor(MC.player.posY / 16.0D) * 16, MathHelper.floor(MC.player.posZ / 16.0D) * 16);
        return playerPos;
    }
}
