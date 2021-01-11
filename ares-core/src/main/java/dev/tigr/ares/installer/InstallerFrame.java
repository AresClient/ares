package dev.tigr.ares.installer;

import javax.swing.*;
import java.io.IOException;

/**
 * @author Tigermouthbear
 */
public class InstallerFrame extends JFrame {
    public static final int WINDOW_WIDTH = 600;
    public static final int WINDOW_HEIGHT = 400;

    InstallerFrame() throws IOException {
        setTitle("Ares Installer");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
        add(new InstallerPanel("/assets/ares/textures/installer.png"));
    }
}
