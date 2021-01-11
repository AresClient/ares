package dev.tigr.ares.installer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static dev.tigr.ares.installer.InstallerFrame.*;

/**
 * @author Tigermouthbear
 */
public class InstallerPanel extends JPanel {
    InstallerPanel(String background) throws IOException {
        setBackground(new Color(0.54f, 0.03f, 0.03f, 1));
        setLayout(null);

        JButton forge = new JButton("Forge");
        JButton fabric = new JButton("Fabric");
        int size = (int) (WINDOW_HEIGHT / 3d);
        int middle = (int) (WINDOW_WIDTH / 2d);
        int padding = (int) (WINDOW_HEIGHT / 6d);
        int padding_w = padding / 2;
        forge.setBounds(middle - size - padding_w, WINDOW_HEIGHT - size - padding, size, size);
        fabric.setBounds(middle + padding_w, WINDOW_HEIGHT - size - padding, size, size);
        add(forge);
        add(fabric);

        JLabel bg = new JLabel(new ImageIcon(ImageIO.read(getClass().getResourceAsStream(background))));
        bg.setLocation(0, 0);
        bg.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        add(bg);
    }
}
