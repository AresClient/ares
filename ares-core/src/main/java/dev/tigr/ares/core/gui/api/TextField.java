package dev.tigr.ares.core.gui.api;

import dev.tigr.ares.core.util.function.DynamicValue;
import dev.tigr.ares.core.util.function.Hook;
import dev.tigr.ares.core.util.render.Color;

import java.util.ArrayList;
import java.util.List;

import static dev.tigr.ares.core.Ares.*;

/**
 * Text field element for entering text
 *
 * @author Tigermouthbear 6/18/20
 */
public class TextField extends Element {
    /**
     * Stores all text fields so that inventory move can be cancelled if any are focused
     */
    private static final List<TextField> fields = new ArrayList<>();

    // TODO: FIX INVENTORY MOVE IN TEXT FIELD

    /**
     * Stores text for the fill in
     */
    private String fillText = "";

    /**
     * Stores the current text in the text field
     */
    private String text = "";

    /**
     * Stores whether the cursor selected the text field and should listen for keystrokes
     */
    private boolean focused = false;

    /**
     * Stores what action to do when enter is pressed in the text field
     */
    private Hook onEnter = () -> {
    };

    /**
     * Stores the color of the text field
     */
    private DynamicValue<Color> color = () -> Color.WHITE;

    /**
     * Constructor for text field which sets color of text field
     *
     * @param color color to set to
     */
    public TextField(GUI gui, DynamicValue<Color> color) {
        this(gui);
        this.color = color;
    }

    /**
     * Constructor for text field which also adds field to fields list
     */
    public TextField(GUI gui) {
        super(gui);
        fields.add(this);
    }

    /**
     * Draw text, cursor, and border of text field
     *
     * @param mouseX       mouse's X position on the screen
     * @param mouseY       mouse's Y position on the screen
     * @param partialTicks partial ticks for rendering
     */
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks); // draw child element's

        // draw border
        RENDERER.drawLineLoop(1, color.getValue(),
                getRenderX(), getRenderY(),
                getRenderX() + getWidth(), getRenderY(),
                getRenderX() + getWidth(), getRenderY() + getHeight(),
                getRenderX(), getRenderY() + getHeight()
        );

        RENDERER.startScissor(getRenderX(), getRenderY(), getWidth(), getHeight());

        // calculate how far left text goes
        double diff = Math.max(FONT_RENDERER.getStringWidth(getText(), getHeight()) + 4 - getWidth(), 0);

        // draw text and get width of text
        double textWidth = FONT_RENDERER.drawStringWithCustomHeightWithShadow(getText(), getRenderX() + 2 - diff, getRenderY(), Color.WHITE, getHeight());

        // draw fill text
        if(!focused && text.isEmpty() && !fillText.isEmpty())
            FONT_RENDERER.drawStringWithCustomHeightWithShadow(fillText, getRenderX() + 2, getRenderY(), Color.WHITE, getHeight());

        // if focused draw blinking cursor
        if(focused) {
            RENDERER.drawLine(
                    getRenderX() + textWidth - diff + 3, getRenderY() + 2,
                    getRenderX() + textWidth - diff + 3, getRenderY() + getHeight() - 2,
                2,
                    new Color(1, 1, 1, System.currentTimeMillis() % 2000 > 1000 ? 1 : 0)
            );
        }

        RENDERER.stopScissor();
    }

    /**
     * Handles whether the text field is focused and listening for keystrokes
     *
     * @param mouseX      mouse's X position on the screen
     * @param mouseY      mouse's Y position on the screen
     * @param mouseButton mouse button clicked
     */
    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        super.click(mouseX, mouseY, mouseButton); // call child element's click methods

        // if mouse is over set focused to true else false
        focused = isMouseOver(mouseX, mouseY) && mouseButton == 0;
    }

    /**
     * Handles the keystrokes for the text field
     *
     * @param typedChar {@link Character} typed
     * @param keyCode   Keycode of character typed
     */
    @Override
    public void keyTyped(Character typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);

        // if focused and key is typed del or add to text or invoke enter listener
        if(focused) {
            if(GUI_MANAGER.isEnterKey(keyCode)) onEnter.invoke();
            else if(GUI_MANAGER.isBackKey(keyCode)) {
                if(getText().length() != 0) setText(getText().substring(0, getText().length() - 1));
            } else if(typedChar != null && GUI_MANAGER.isChatAllowed(typedChar)) setText(getText() + typedChar);
        }
    }

    /**
     * Unfocus from field when GUI is closed
     */
    @Override
    public void close() {
        super.close();
        focused = false;
    }

    /**
     * Getter for text in field
     *
     * @return text in field
     */
    public String getText() {
        return text;
    }

    /**
     * Setter for text in field
     *
     * @param value text
     */
    public void setText(String value) {
        text = value;
    }

    /**
     * Sets the fill text
     *
     * @param value text
     */
    public void setFillText(String value) {
        fillText = value;
    }

    /**
     * Setter for the enter hook
     *
     * @param value funcional interface to set to
     */
    public void setOnEnterHook(Hook value) {
        onEnter = value;
    }
}
