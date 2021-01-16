package dev.tigr.ares.installer;

import javax.swing.*;
import java.awt.*;

import static dev.tigr.ares.installer.Installer.*;

/**
 * @author Tigermouthbear
 */
public class InstallPanel extends JPanel {
    InstallPanel(Installer.Version version) {
        setBackground(new Color(0.54f, 0.03f, 0.03f, 1)); // just in case
        setLayout(null);

        // add options panel
        int width = WINDOW_WIDTH / 2;
        int height = WINDOW_HEIGHT / 5;
        InstallOptionsPanel panel = new InstallOptionsPanel(version);
        panel.setBounds(WINDOW_WIDTH / 2 - width / 2, WINDOW_HEIGHT / 2 - height / 2, width, height);
        add(panel);
    }

    @Override
    protected void paintChildren(Graphics graphics) {
        // draw background
        graphics.drawImage(BACKGROUND, 0, 0, this);

        super.paintChildren(graphics);
    }
}
