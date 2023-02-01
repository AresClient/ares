package org.aresclient.ares.mixins;

import net.minecraft.client.gui.screen.Screen;
import org.aresclient.ares.api.ScreenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Screen.class)
public class MixinScreen implements ScreenContext {
    @Shadow public int width;

    @Shadow public int height;

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }
}
