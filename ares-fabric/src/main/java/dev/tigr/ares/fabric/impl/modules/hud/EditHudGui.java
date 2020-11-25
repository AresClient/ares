package dev.tigr.ares.fabric.impl.modules.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * @author Tigermouthbear
 */
public class EditHudGui extends Screen implements Wrapper {
    private static final ArrayList<HudElement> elements = new ArrayList<>();

    private final Screen prevScreen;

    public EditHudGui(Screen prevScreen) {
        super(new LiteralText("Ares HUD Editor"));

        this.prevScreen = prevScreen;
        elements.clear();
        Category.HUD.getModules().stream().filter(module -> module instanceof HudElement).forEach(module -> elements.add((HudElement) module));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1, 1, 1, 1);
        RenderSystem.enableAlphaTest();

        elements.stream().filter(Module::getEnabled).forEach(hudElement -> {
            hudElement.onEditDraw(mouseX, mouseY, this);
            hudElement.draw();
        });

        // hud editor text
        double height = this.height / 30d;
        double width = FONT_RENDERER.getStringWidth("HUD Editor", height);
        double x = this.width / 2d - width / 2;
        double y = 0;
        RENDERER.drawRect(x - 2, y, width + 4, height + 2, Color.BLACK);
        RENDERER.drawLineLoop(1, ClickGUIMod.getColor(),
                x - 2, y,
                x - 2, y + height + 2,
                x + width + 2, y + height + 2,
                x + width + 2, y
        );
        FONT_RENDERER.drawStringWithCustomHeight("HUD Editor", x, y + 2, Color.WHITE, height);

        String tooltip = "";
        for(HudElement hudElement: elements) {
            if(hudElement.isMouseOver(mouseX, mouseY) && hudElement.getEnabled()) tooltip = hudElement.getName();
        }
        if(!tooltip.isEmpty()) RENDERER.drawTooltip(tooltip, mouseX, mouseY, ClickGUIMod.getColor());

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.popMatrix();

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        HudElement elementClicked = null;
        for(int i = (int) elements.stream().filter(Module::getEnabled).count() - 1; i >= 0; i--) {
            HudElement hudElement = elements.get(elements.indexOf(elements.stream().filter(Module::getEnabled).collect(Collectors.toList()).get(i)));
            if(hudElement.isMouseOver(mouseX, mouseY) && hudElement.getEnabled()) {
                hudElement.onClick(mouseX, mouseY, mouseButton);
                elementClicked = hudElement;
                break;
            }
        }

        if(elementClicked != null) {
            elements.remove(elementClicked);
            elements.add(elementClicked);
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        elements.stream().filter(Module::getEnabled).forEach(hudElement -> hudElement.onRelease(mouseX, mouseY, mouseButton));
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if(keyCode == 27) MC.openScreen(prevScreen);
        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
