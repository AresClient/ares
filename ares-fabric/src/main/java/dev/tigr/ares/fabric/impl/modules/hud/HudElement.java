package dev.tigr.ares.fabric.impl.modules.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.SettingCategory;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.fabric.impl.modules.hud.elements.TextShadow;
import dev.tigr.ares.fabric.impl.util.CustomGUIManager;
import net.minecraft.client.gui.screen.Screen;

/**
 * @author Tigermouthbear
 */
public abstract class HudElement extends Module {
    protected static final Color GRAY = new Color(0.5f, 0.5f, 0.5f, 0.3f);
    protected static final SettingCategory hudSettings = new SettingCategory("Hud");

    protected final Setting<Background> background = register(new EnumSetting<>("Background", Background.NONE));

    private final Setting<Integer> x;
    private final Setting<Integer> y;
    private final Setting<Integer> width;
    private final Setting<Integer> height;
    private boolean isDragging = false;
    private double diffX = 0;
    private double diffY = 0;

    public HudElement(int x, int y, int width, int height) {
        SettingCategory category = new SettingCategory(hudSettings, getName());
        this.x = new IntegerSetting(category, "x", x, 0, 1000);
        this.y = new IntegerSetting(category, "y", y, 0, 1000);
        this.width = new IntegerSetting(category, "width", width, 0, 1000);
        this.height = new IntegerSetting(category, "height", height, 0, 1000);

        Ares.EVENT_MANAGER.register(this);
    }

    void onClick(double mouseX, double mouseY, int mouseButton) {
        if(mouseButton == 0 && isMouseOver(mouseX, mouseY)) {
            isDragging = true;
            diffX = mouseX - getX();
            diffY = mouseY - getY();
        }
    }

    void onEditDraw(double mouseX, double mouseY, Screen screen) {
        if(isDragging) {
            setX((int) (mouseX - diffX));
            setY((int) (mouseY - diffY));
        }

        RENDERER.drawRect(getX(), getY(), getWidth(), getHeight(), GRAY);
    }

    void onRelease(double mouseX, double mouseY, int mouseButton) {
        isDragging = false;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY <= getY() + getHeight();
    }

    public abstract void draw();

    protected void drawBackground() {
        if(background.getValue() != Background.NONE) RENDERER.drawRect(getX(), getY(), getWidth(), getHeight(), Color.BLACK);
        if(background.getValue() == Background.FANCY || background.getValue() == Background.RAINBOW) RENDERER.drawLineLoop(1, background.getValue() == Background.RAINBOW ? IRenderer.rainbow() : ClickGUIMod.getColor(),
                getX(), getY(),
                getX() + getWidth(), getY(),
                getX() + getWidth(), getY() + getHeight(),
                getX(), getY() + getHeight()
        );
    }

    @Override
    public void onRender2d() {
        if(!(MC.currentScreen instanceof EditHudGui) && !(MC.currentScreen instanceof CustomGUIManager)) {
            drawBackground();
            //RenderSystem.color4f(1, 1, 1, 1);
            draw();
        }
    }

    public int getX() {
        return x.getValue();
    }

    public void setX(int value) {
        x.setValue(value);
    }

    public int getY() {
        return y.getValue();
    }

    public void setY(int value) {
        y.setValue(value);
    }

    public int getWidth() {
        return width.getValue();
    }

    public void setWidth(int value) {
        width.setValue(value);
    }

    public int getHeight() {
        return height.getValue();
    }

    public void setHeight(int value) {
        height.setValue(value);
    }

    protected void drawString(String text, double x, double y, Color color) {
        FONT_RENDERER.drawString(text, x, y, color, TextShadow.INSTANCE.getEnabled());
    }

    public enum Background { NONE, RAINBOW, FANCY, SIMPLE }
}
