package dev.tigr.ares.forge.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.forge.impl.modules.hud.EditHudGui;
import dev.tigr.ares.forge.impl.modules.hud.HudElement;
import dev.tigr.ares.forge.utils.Comparators;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "ModuleList", description = "Displays a list of all modules enabled", category = Category.HUD, enabled = true, visible = false)
public class ModuleList extends HudElement {
    private final Setting<Mode> mode = register(new EnumSetting<>("Order", Mode.LENGTH));
    private final Setting<Boolean> rainbow = register(new BooleanSetting("Rainbow", false));

    public ModuleList() {
        super(0, 50, 20, 10);
    }

    @Override
    protected void drawBackground() {  }

    public void draw() {
        int pos = 0;
        AtomicInteger biggestWidth = new AtomicInteger(20);

        // points for line looping in fancy background
        List<Vec3d> points = new ArrayList<>();
        boolean shouldRenderFancy = background.getValue() == Background.FANCY && !(MC.currentScreen instanceof EditHudGui);

        List<Module> modules = Module.MANAGER.getInstances().stream().filter(module -> module.getEnabled() && module.isVisible()).collect(Collectors.toList());
        modules.sort(mode.getValue().comparator);

        ScaledResolution resolution = new ScaledResolution(MC);

        if(getX() + getWidth() / 2 <= resolution.getScaledWidth() / 2) {
            for(Module module: modules) {
                drawModule(module, getX(), getY() + pos);

                double width = FONT_RENDERER.getStringWidth(module.getHudName());
                if(width > biggestWidth.get()) biggestWidth.set((int) width);

                if(shouldRenderFancy) {
                    points.add(new Vec3d(getX() + width + 1, getY() + pos - 1, 0));
                    points.add(new Vec3d(getX() + width + 1, getY() + pos + FONT_RENDERER.getFontHeight(), 0));
                }

                pos += FONT_RENDERER.getFontHeight() + 1;
            }

            if(shouldRenderFancy) {
                points.add(new Vec3d(getX() - 1, getY() + pos - 1, 0));
                points.add(new Vec3d(getX() - 1, getY() - 1, 0));
            }
        } else if(getX() + getWidth() / 2 > resolution.getScaledWidth() / 2) {
            modules.forEach(module -> {
                if(FONT_RENDERER.getStringWidth(module.getHudName()) >= biggestWidth.get())
                    biggestWidth.set((int) FONT_RENDERER.getStringWidth(module.getHudName()));
            });

            for(Module module: modules) {
                double width = FONT_RENDERER.getStringWidth(module.getHudName());
                drawModule(module, getX() + biggestWidth.get() - width, getY() + pos);

                if(shouldRenderFancy) {
                    points.add(new Vec3d(getX() + biggestWidth.get() - width - 1, getY() + pos - 1, 0));
                    points.add(new Vec3d(getX() + biggestWidth.get() - width - 1, getY() + pos + FONT_RENDERER.getFontHeight(), 0));
                }

                pos += FONT_RENDERER.getFontHeight() + 1;
            }

            if(shouldRenderFancy) {
                points.add(new Vec3d(getX() + getWidth() + 1, getY() + pos - 1, 0));
                points.add(new Vec3d(getX() + getWidth() + 1, getY() - 1, 0));
            }
        }

        if(shouldRenderFancy) {
            Color color = IRenderer.rainbow();
            Vec3d last = null;
            for(Vec3d vec3d: points) {
                if(last != null) RENDERER.drawLine(last.x, last.y, vec3d.x, vec3d.y, 1, color);
                last = vec3d;
            }
            Vec3d first = points.get(0);
            RENDERER.drawLine(last.x, last.y, first.x, first.y, 1, color);
        }

        setHeight(pos);
        setWidth(biggestWidth.get());
    }

    private void drawModule(Module module, double x, double y) {
        if(background.getValue() != Background.NONE && !(MC.currentScreen instanceof EditHudGui)) RENDERER.drawRect(x - 1, y - 1, FONT_RENDERER.getStringWidth(module.getHudName()) + 2, FONT_RENDERER.getFontHeight() + 1, Color.BLACK);
        drawString(module.getHudName(), x, y, rainbow.getValue() ? IRenderer.rainbow() : Color.WHITE);
    }

    enum Mode {
        LENGTH(Comparators.moduleStrLength),
        ALPHABETIC(Comparators.moduleAlphabetic);

        private final Comparator comparator;

        Mode(Comparator comparator) {
            this.comparator = comparator;
        }
    }
}
