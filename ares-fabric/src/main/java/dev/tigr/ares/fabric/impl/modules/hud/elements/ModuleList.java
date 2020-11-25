package dev.tigr.ares.fabric.impl.modules.hud.elements;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.fabric.impl.modules.hud.HudElement;
import dev.tigr.ares.fabric.utils.Comparators;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 8/28/20
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

        List<Module> modules = Module.MANAGER.getInstances().stream().filter(module -> module.getEnabled() && module.isVisible()).collect(Collectors.toList());
        modules.sort(mode.getValue().comparator);

        if(getX() + getWidth() / 2d <= MC.getWindow().getScaledWidth() / 2d) {
            for(Module module: modules) {
                drawModule(module, getX(), getY() + pos);

                double width = FONT_RENDERER.getStringWidth(module.getHudName());
                if(width > biggestWidth.get()) biggestWidth.set((int) width);

                if(background.getValue() == Background.FANCY) {
                    points.add(new Vec3d(getX() + width + 1, getY() + pos - 1, 0));
                    points.add(new Vec3d(getX() + width + 1, getY() + pos + FONT_RENDERER.getFontHeight(), 0));
                }

                pos += FONT_RENDERER.getFontHeight() + 1;
            }

            if(background.getValue() == Background.FANCY) {
                points.add(new Vec3d(getX() - 1, getY() + pos - 1, 0));
                points.add(new Vec3d(getX() - 1, getY() - 1, 0));
            }
        } else if(getX() + getWidth() / 2d > MC.getWindow().getScaledWidth() / 2d) {
            modules.forEach(module -> {
                if(FONT_RENDERER.getStringWidth(module.getHudName()) >= biggestWidth.get())
                    biggestWidth.set((int) FONT_RENDERER.getStringWidth(module.getHudName()));
            });

            for(Module module: modules) {
                double width = FONT_RENDERER.getStringWidth(module.getHudName());
                drawModule(module, getX() + biggestWidth.get() - width, getY() + pos);

                if(background.getValue() == Background.FANCY) {
                    points.add(new Vec3d(getX() + biggestWidth.get() - width - 1, getY() + pos - 1, 0));
                    points.add(new Vec3d(getX() + biggestWidth.get() - width - 1, getY() + pos + FONT_RENDERER.getFontHeight(), 0));
                }

                pos += FONT_RENDERER.getFontHeight() + 1;
            }

            if(background.getValue() == Background.FANCY) {
                points.add(new Vec3d(getX() + getWidth() + 1, getY() + pos - 1, 0));
                points.add(new Vec3d(getX() + getWidth() + 1, getY() - 1, 0));
            }
        }

        if(background.getValue() == Background.FANCY) {
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
        if(background.getValue() != Background.NONE) RENDERER.drawRect(x - 1, y - 1, FONT_RENDERER.getStringWidth(module.getHudName()) + 2, FONT_RENDERER.getFontHeight() + 1, Color.BLACK);
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