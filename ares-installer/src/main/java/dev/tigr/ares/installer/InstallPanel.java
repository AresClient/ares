package dev.tigr.ares.installer;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static dev.tigr.ares.installer.Installer.*;

/**
 * @author Tigermouthbear
 */
public class InstallPanel extends JPanel {
    InstallPanel(List<Candidate> candidates) {
        setBackground(new Color(0.098f, 0.098f, 0.098f, 1)); // just in case
        setLayout(null);

        // add options panel
        int height = WINDOW_HEIGHT / 2;
        InstallOptionsPanel panel = new InstallOptionsPanel(candidates);
        panel.setBounds(0, WINDOW_HEIGHT / 2 - height / 5, WINDOW_WIDTH, height);
        add(panel);
    }

    @Override
    protected void paintChildren(Graphics graphics) {
        // draw background
        graphics.drawImage(BACKGROUND, 0, 0, this);

        super.paintChildren(graphics);
    }
}
