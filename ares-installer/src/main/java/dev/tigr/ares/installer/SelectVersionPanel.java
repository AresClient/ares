package dev.tigr.ares.installer;

import javax.swing.*;
import java.awt.*;

import static dev.tigr.ares.installer.Installer.*;

/**
 * @author Tigermouthbear
 */
public class SelectVersionPanel extends JPanel {
    private static final Font FONT = new Font("Arial", Font.PLAIN, 24);

    private final JButton forge;
    private final JButton fabric;

    SelectVersionPanel() {
        setBackground(new Color(0.098f, 0.098f, 0.098f, 1)); // just in case
        setLayout(null);

        // calculate button positions
        int size = (int) (WINDOW_HEIGHT / 3d);
        int middle = (int) (WINDOW_WIDTH / 2d);
        int padding = (int) (WINDOW_HEIGHT / 6d);
        int padding_w = padding / 2;

        // create buttons
        forge = new JButton(new ImageIcon(getImage("forge.png", size, size)));
        fabric = new JButton(new ImageIcon(getImage("fabric.png", size, size)));

        // set positions
        forge.setBounds(middle - size - padding_w, (int) (WINDOW_HEIGHT - size - padding * 1.3), size, size);
        fabric.setBounds(middle + padding_w, (int) (WINDOW_HEIGHT - size - padding * 1.3), size, size);

        // remove border and set cursor
        forge.setBorder(BorderFactory.createEmptyBorder());
        fabric.setBorder(BorderFactory.createEmptyBorder());
        forge.setContentAreaFilled(false);
        fabric.setContentAreaFilled(false);
        forge.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fabric.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // add event listeners
        forge.addActionListener(event -> Installer.INSTANCE.select(Version.FORGE));
        fabric.addActionListener(event -> Installer.INSTANCE.select(Version.FABRIC));

        // add to panel
        add(forge);
        add(fabric);
    }

    @Override
    protected void paintChildren(Graphics graphics) {
        // draw background
        graphics.drawImage(BACKGROUND, 0, 0, this);

        // turn on anti aliasing
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // draw version text
        FontMetrics fontMetrics = graphics.getFontMetrics(FONT);
        int fontHeight = fontMetrics.getHeight();
        graphics.setFont(FONT);
        graphics.setColor(Color.WHITE);
        graphics.drawString(FORGE_MCVERSION, (int) (forge.getX() + forge.getWidth() / 2 - fontMetrics.getStringBounds(FORGE_MCVERSION, graphics).getWidth() / 2), forge.getY() + forge.getHeight() + fontHeight);
        graphics.drawString(FABRIC_MCVERSION, (int) (fabric.getX() + fabric.getWidth() / 2 - fontMetrics.getStringBounds(FABRIC_MCVERSION, graphics).getWidth() / 2), fabric.getY() + fabric.getHeight() + fontHeight);

        super.paintChildren(graphics);
    }
}
