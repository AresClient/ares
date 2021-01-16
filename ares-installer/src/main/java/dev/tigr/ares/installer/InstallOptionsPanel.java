package dev.tigr.ares.installer;

import javax.swing.*;
import java.awt.*;

import static java.awt.Frame.getFrames;

public class InstallOptionsPanel extends JPanel {
    private final Installer.Version version;

    InstallOptionsPanel(Installer.Version version) {
        this.version = version;

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        
        // create button
        JButton folderButton = new JButton("Select mods folder");
        folderButton.addActionListener(event -> {
            // add folder selector
            FileDialog fileDialog = new FileDialog(getFrames()[0]);
            fileDialog.setMode(FileDialog.LOAD);
            fileDialog.setFilenameFilter((file, s) -> file.isDirectory());
            fileDialog.setVisible(true);
        });
        add(folderButton, c);

        JLabel label = new JLabel(version.name().toLowerCase());
        c.gridx = 1;
        add(label, c);
    }
    
}
