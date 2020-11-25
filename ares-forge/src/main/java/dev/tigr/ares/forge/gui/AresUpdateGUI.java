package dev.tigr.ares.forge.gui;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.util.global.UpdateHelper;
import dev.tigr.ares.core.util.render.TextColor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
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
public class AresUpdateGUI extends GuiScreen {
    @Override
    public void initGui() {
        this.buttonList.add(new GuiButton(0, this.width / 2 - 50 - 105, this.height / 6 + 96, 100, 20, "Download"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 50, this.height / 6 + 96, 100, 20, "View Changelog"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 50 + 105, this.height / 6 + 96, 100, 20, "Skip"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(this.fontRenderer, "Ares " + TextColor.BLUE + UpdateHelper.getLatestVersion() + TextColor.WHITE + " has been released. You are still on version " + TextColor.BLUE + Ares.VERSION_FULL, this.width / 2, 110, 16764108);
        drawCenteredString(this.fontRenderer, "Would you like to download it, view the changelog, or skip for now?", this.width / 2, 120, 16764108);
    }

    @Override
    public void actionPerformed(GuiButton guiButton) {
        switch(guiButton.id) {
            case 0:
                openLink("https://aresclient.org/download");
                break;

            case 1:
                openLink("https://aresclient.org/changelogs/" + UpdateHelper.getLatestVersion() + ".txt");
                break;

            default:
                mc.displayGuiScreen(new GuiMainMenu());
                break;
        }
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
