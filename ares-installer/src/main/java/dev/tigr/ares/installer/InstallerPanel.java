package dev.tigr.ares.installer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static dev.tigr.ares.installer.Installer.*;

/**
 * @author Tigermouthbear
 */
public class InstallerPanel extends JPanel {
    InstallerPanel() {
        setBackground(new Color(0.54f, 0.03f, 0.03f, 1)); // just in case
        setLayout(null);

        // calculate button positions
        int size = (int) (WINDOW_HEIGHT / 3d);
        int middle = (int) (WINDOW_WIDTH / 2d);
        int padding = (int) (WINDOW_HEIGHT / 6d);
        int padding_w = padding / 2;

        // create buttons
        JButton forge = new JButton(getTexture("forge.png", size, size));
        JButton fabric = new JButton(getTexture("fabric.png", size, size));

        // set positions
        forge.setBounds(middle - size - padding_w, WINDOW_HEIGHT - size - padding, size, size);
        fabric.setBounds(middle + padding_w, WINDOW_HEIGHT - size - padding, size, size);

        // remove border
        forge.setBorder(BorderFactory.createEmptyBorder());
        fabric.setBorder(BorderFactory.createEmptyBorder());
        forge.setContentAreaFilled(false);
        fabric.setContentAreaFilled(false);

        // add event listeners
        forge.addActionListener(event -> Installer.install(Version.FORGE));
        fabric.addActionListener(event -> Installer.install(Version.FABRIC));

        // add to panel
        add(forge);
        add(fabric);

        JLabel bg = new JLabel(getTexture("background.png"));
        bg.setLocation(0, 0);
        bg.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        add(bg);
    }

    private ImageIcon getTexture(String name) {
        try {
            return new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/assets/ares/installer/" + name)));
        } catch(IOException e) {
            e.printStackTrace();
        }
        return new ImageIcon();
    }

    private ImageIcon getTexture(String name, int width, int height) {
        return new ImageIcon(getTexture(name).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }
}
