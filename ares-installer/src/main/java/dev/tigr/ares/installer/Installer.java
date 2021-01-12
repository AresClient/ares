package dev.tigr.ares.installer;

import javax.swing.*;
import java.io.IOException;

/**
 * @author Tigermouthbear
 */
public class Installer extends JFrame {
    enum Version { FORGE, FABRIC }

    public static final int WINDOW_WIDTH = 600;
    public static final int WINDOW_HEIGHT = 400;

    Installer() throws IOException {
        setTitle("Ares Installer");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
        add(new InstallerPanel());
    }

    public static void main(String[] args) throws IOException {
        new Installer();
    }

    static void install(Version version) {
        System.out.println("Installing Ares " + version.name().toLowerCase() + "...");
    }
}
