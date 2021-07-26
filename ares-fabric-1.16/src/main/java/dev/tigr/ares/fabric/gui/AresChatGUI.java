package dev.tigr.ares.fabric.gui;

import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.fabric.mixin.accessors.ChatScreenAccessor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/**
 * @author Tigermouthbear 10/16/20
 */
public class AresChatGUI extends ChatScreen {
    public AresChatGUI(String prefix) {
        super(prefix);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if(chatField.getText().startsWith(Command.PREFIX.getValue())) {
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
                GL11.glVertex2d(width - 2, height - 14);

                // bottom
                GL11.glVertex2d(2, height - 2);
                GL11.glVertex2d(width - 2, height - 2);

                // left
                GL11.glVertex2d(2, height - 14);
                GL11.glVertex2d(2, height - 2);

                // right
                GL11.glVertex2d(width - 2, height - 14);
                GL11.glVertex2d(width - 2, height - 2);

            }
            GL11.glEnd();

            if(blend) GL11.glEnable(GL11.GL_BLEND);
            if(texture2d) GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        this.setFocused(this.chatField);
        this.chatField.setTextFieldFocused(true);
        fill(matrixStack, 2, this.height - 14, this.width - 2, this.height - 2, client.options.getTextBackgroundColor(Integer.MIN_VALUE));
        this.chatField.render(matrixStack, mouseX, mouseY, partialTicks);

        if(chatField.getText().startsWith(Command.PREFIX.getValue()))
            textRenderer.drawWithShadow(matrixStack, Command.complete(chatField.getText()), 4 + textRenderer.getWidth(chatField.getText()), chatField.y, 7368816);

        ((ChatScreenAccessor) this).getCommandSuggestor().render(matrixStack, mouseX, mouseY);

        Style style = this.client.inGameHud.getChatHud().getText(mouseX, mouseY);
        if (style != null && style.getHoverEvent() != null) {
            this.renderTextHoverEffect(matrixStack, style, mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keycode, int j, int k) {
        if(keycode == GLFW.GLFW_KEY_TAB && !chatField.getText().contains(" ")) {
            String noPrefix = chatField.getText().replaceFirst(Command.PREFIX.getValue(), "");
            String completed = Command.completeName(noPrefix);
            chatField.setText(chatField.getText() + completed.replaceFirst(noPrefix, ""));
        }

        return super.keyPressed(keycode, j, k);
    }
}
