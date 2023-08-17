package org.aresclient.ares.impl.minecraft.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.aresclient.ares.api.minecraft.render.Screen;

public class ScreenAdapter extends net.minecraft.client.gui.screen.Screen {
    private final Screen screen;

    public ScreenAdapter(Screen screen) {
        super(Text.literal(screen.getTitle()));
        RenderSystem.assertOnRenderThreadOrInit();
        this.screen = screen;
    }

    @Override
    public void init() {
        screen.resize(width, height);
        screen.update();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        screen.render(mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        screen.click((int) mouseX, (int) mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        screen.release((int) mouseX, (int) mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        screen.type(null, keyCode);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int keycode) {
        screen.type(character, keycode);
        return super.charTyped(character, keycode);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double value) {
        screen.scroll((int) mouseX, (int) mouseY, value * 15);
        return super.mouseScrolled(mouseX, mouseY, value);
    }

    @Override
    public boolean shouldPause() {
        return screen.shouldPause();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return screen.shouldCloseOnEsc();
    }

    @Override
    public void close() {
        screen.close();
        super.close();
    }
}
