package dev.tigr.ares.fabric.gui;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.util.global.UpdateHelper;
import dev.tigr.ares.core.util.render.TextColor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The update notification GUI
 *
 * @author Tigermouthbear 7/9/20
 */
public class AresUpdateGUI extends Screen implements Wrapper {
    public AresUpdateGUI() {
        super(new LiteralText("Ares Update Notifier"));
    }

    @Override
    public void init() {
        addButton(new ButtonWidget(this.width / 2 - 50 - 105, this.height / 6 + 96, 100, 20, new LiteralText("Download"), button -> openLink("https://aresclient.org/download")));
        addButton(new ButtonWidget(this.width / 2 - 50, this.height / 6 + 96, 100, 20, new LiteralText("View Changelog"), button -> openLink("https://aresclient.org/changelogs/" + UpdateHelper.getLatestVersion() + ".txt")));
        addButton(new ButtonWidget(this.width / 2 - 50 + 105, this.height / 6 + 96, 100, 20, new LiteralText("Skip"), button -> MC.openScreen(new AresMainMenu())));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        drawCenteredString(matrixStack, client.textRenderer, "Ares " + TextColor.BLUE + UpdateHelper.getLatestVersion() + TextColor.WHITE + " has been released. You are still on version " + TextColor.BLUE + Ares.VERSION_FULL, this.width / 2, 110, 16764108);
        drawCenteredString(matrixStack, client.textRenderer, "Would you like to download it, view the changelog, or skip for now?", this.width / 2, 120, 16764108);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    // https://stackoverflow.com/questions/27879854/desktop-getdesktop-browse-hangs
    public void openLink(String urlString) {
        try {
            if(SystemUtils.IS_OS_LINUX) {
                // Workaround for Linux because "Desktop.getDesktop().browse()" doesn't work on some Linux implementations
                if(Runtime.getRuntime().exec(new String[]{"which", "xdg-open"}).getInputStream().read() != -1) {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", urlString});
                }
            } else {
                if(Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(urlString));
                }
            }

        } catch(IOException | URISyntaxException ignored) {
        }
    }
}
