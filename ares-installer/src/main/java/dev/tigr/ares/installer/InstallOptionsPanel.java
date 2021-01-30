package dev.tigr.ares.installer;

import li.flor.nativejfilechooser.NativeJFileChooser;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static dev.tigr.ares.installer.Installer.*;
import static java.awt.Frame.getFrames;

/**
 * @author Tigermouthbear
 */
public class InstallOptionsPanel extends JPanel {
    private File minecraftFolder;

    InstallOptionsPanel(Installer.Version version) {
        // try to find mods folder
        String folder = getMinecraftFolder();
        minecraftFolder = folder != null ? new File(folder) : null;
        if(minecraftFolder != null && (!minecraftFolder.exists() || !minecraftFolder.isDirectory())) minecraftFolder = null;

        setBackground(new Color(0.098f, 0.098f, 0.098f, 1));

        // set layout
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        
        // create elements
        Font optionFont = new Font("Arial", Font.PLAIN, 16);
        JLabel label = new JLabel("Ares " + getVersion(version) + " " + version.name().toLowerCase().replaceFirst("f", "F") + " " + getMinecraftVersion(version), SwingConstants.CENTER);
        JButton folderButton = new JButton("Select minecraft folder");
        JLabel folderLabel = new JLabel(minecraftFolder != null ? minecraftFolder.getPath() : "unknown");
        JButton installButton = new JButton("Install Latest");
        JButton backButton = new JButton("Back");

        // add label
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.insets = new Insets(3, 3, 10, 3);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.PLAIN, 32));
        add(label, c);
        c.gridwidth = 1;

        // add folder button
        folderButton.addActionListener(event -> {
            // add folder selector
            NativeJFileChooser fileChooser = new NativeJFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if(fileChooser.showOpenDialog(getFrames()[0]) == JFileChooser.APPROVE_OPTION) {
                minecraftFolder = fileChooser.getSelectedFile();
                String text = minecraftFolder.getPath();
                text = text.length() > 50 ? text.substring(text.length() - 50) : text;
                folderLabel.setText(text);
                update(getGraphics());
            }
        });
        c.gridy = 1;
        folderButton.setFont(optionFont);
        folderButton.setForeground(Color.BLACK);
        add(folderButton, c);

        // add label
        folderLabel.setForeground(Color.WHITE);
        folderLabel.setFont(optionFont);
        c.insets = new Insets(3,10,10,3);
        c.gridx = 1;
        add(folderLabel, c);

        // add install button
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(10, 0, 0, 0);
        c.gridwidth = 2;
        installButton.setPreferredSize(new Dimension(WINDOW_WIDTH / 20, WINDOW_HEIGHT / 10));
        installButton.setFont(installButton.getFont().deriveFont(22f));
        installButton.addActionListener(actionEvent -> {
            JOptionPane.showMessageDialog(null, Installer.install(version, minecraftFolder), "Ares Client", JOptionPane.INFORMATION_MESSAGE);
        });
        add(installButton, c);

        // add back button
        c.gridy = 3;
        backButton.setFont(backButton.getFont().deriveFont(15f));
        backButton.addActionListener(actionEvent -> INSTANCE.home());
        add(backButton, c);
    }
}
