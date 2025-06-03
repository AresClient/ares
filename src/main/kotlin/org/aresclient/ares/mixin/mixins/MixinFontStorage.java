package org.aresclient.ares.mixin.mixins;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderLayerSet;
import net.minecraft.util.Identifier;
import org.aresclient.ares.api.nrender.font.CustomFontStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FontStorage.class)
public class MixinFontStorage {
    @Redirect(method = "bake(Lnet/minecraft/client/font/RenderableGlyph;)Lnet/minecraft/client/font/BakedGlyph;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderLayerSet;of(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/font/TextRenderLayerSet;"))
    private TextRenderLayerSet renderLayerSetOf(Identifier textureId) {
        if(((Object) this) instanceof CustomFontStorage) return CustomFontStorage.renderLayerSetOf(textureId);
        else return TextRenderLayerSet.of(textureId);
    }

    @Redirect(method = "bake(Lnet/minecraft/client/font/RenderableGlyph;)Lnet/minecraft/client/font/BakedGlyph;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderLayerSet;ofIntensity(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/font/TextRenderLayerSet;"))
    private TextRenderLayerSet renderLayerSetOfIntensity(Identifier textureId) {
        if(((Object) this) instanceof CustomFontStorage) return CustomFontStorage.renderLayerSetOfIntensity(textureId);
        else return TextRenderLayerSet.ofIntensity(textureId);
    }
}
