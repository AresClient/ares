package dev.tigr.ares.forge.gui;

import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * @author Tigermouthbear
 */
public class AresChatGUI extends GuiChat {
    public AresChatGUI(String prefix) {
        super(prefix);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if(inputField.getText().startsWith(Command.PREFIX.getValue())) {
            boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
            boolean texture2d = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(Color.ARES_RED.getRed(), Color.ARES_RED.getGreen(), Color.ARES_RED.getBlue(), Color.ARES_RED.getAlpha());
            GL11.glLineWidth(2);
            GL11.glBegin(GL11.GL_LINES);
            {
                // top
                GL11.glVertex2d(2, height - 14);
                GL11.glVertex2d(width, height - 14);

                // bottom
                GL11.glVertex2d(2, height - 2);
                GL11.glVertex2d(width, height - 2);

                // left
                GL11.glVertex2d(2, height - 14);
                GL11.glVertex2d(2, height - 2);

                // right
                GL11.glVertex2d(width, height - 14);
                GL11.glVertex2d(width, height - 2);

            }
            GL11.glEnd();

            if(blend) GL11.glEnable(GL11.GL_BLEND);
            if(texture2d) GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        drawRect(2, height - 14, width - 2, height - 2, Integer.MIN_VALUE);
        inputField.drawTextBox();

        if(inputField.getText().startsWith(Command.PREFIX.getValue()))
            fontRenderer.drawStringWithShadow(Command.complete(inputField.getText()), 4 + fontRenderer.getStringWidth(inputField.getText()), inputField.y, Color.GRAY.getRGB());

        ITextComponent itextcomponent = mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if(itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
            handleComponentHover(itextcomponent, mouseX, mouseY);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if(keyCode == Keyboard.KEY_TAB && !inputField.getText().contains(" ")) {
            String noPrefix = inputField.getText().replaceFirst(Command.PREFIX.getValue(), "");
            String completed = Command.completeName(noPrefix);
            inputField.setText(inputField.getText() + completed.replaceFirst(noPrefix, ""));
        }
    }
}
