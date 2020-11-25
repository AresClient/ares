package dev.tigr.ares.core.gui.impl.game.window.windows;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.client.SystemChatMessageEvent;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.gui.api.GUI;
import dev.tigr.ares.core.gui.api.TextField;
import dev.tigr.ares.core.gui.impl.game.window.Window;
import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

import java.util.ArrayList;
import java.util.List;

import static dev.tigr.ares.core.Ares.*;

/**
 * @author Tigermouthbear 6/17/20
 */
public class ConsoleWindow extends Window {
    private final List<String> lines = new ArrayList<>();
    private double height = 0;
    private double scrollOffset = 0;
    @EventHandler
    public EventListener<SystemChatMessageEvent> systemChatMessageEvent = new EventListener<>(event -> {
        lines.add(event.getText());
        scrollOffset = 0;
    });

    public ConsoleWindow(GUI gui, DynamicValue<Color> color) {
        super(gui, "Console", color, false, 0.64, 0.1);

        Ares.EVENT_MANAGER.register(this);

        // set width and height
        setWidth(() -> getScreenWidth() / 3d);
        setHeight(() -> getScreenHeight() / 3d);

        TextField input = new TextField(getGUI(), color);
        input.setOnEnterHook(() -> {
            Command.execute(input.getText());
            input.setText("");
        });
        input.setY(() -> getHeight() - getBarHeight());
        input.setHeight(this::getBarHeight);
        input.setWidth(this::getWidth);
        input.setFillText("Enter a command...");
        add(input);
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        // scissor area
        RENDERER.startScissor(getRenderX(), getRenderY() + getBarHeight(), getWidth(), getHeight() - getBarHeight() * 2);

        // transform for scroll
        RENDER_STACK.translate(0, scrollOffset, 0);

        // render lines
        double y = getRenderY() - height + getHeight() - getBarHeight();
        height = 0;
        for(String line: lines.size() > 51 ? lines.subList(lines.size() - 1 - 50, lines.size() - 1) : new ArrayList<>(lines)) {
            double textHeight = FONT_RENDERER.drawSplitString(line, getRenderX() + 2, y, Color.WHITE, getWidth() - 4, getBarHeight());
            y += textHeight;
            height += textHeight;
        }

        // undo scroll transform
        RENDER_STACK.translate(0, -scrollOffset, 0);

        // pop attrib
        RENDERER.stopScissor();
    }

    @Override
    public void scroll(double mouseX, double mouseY, double value) {
        super.scroll(mouseX, mouseY, value);

        if(value != 0) {
            value /= 10;
            scrollOffset += value;
        }
    }
}
