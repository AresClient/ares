package dev.tigr.ares.installer;

import org.json.JSONObject;
import org.json.JSONTokener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Tigermouthbear
 */
public class Installer extends JFrame {
    private static final String URL = "https://aresclient.org/api/v1/downloads.json";

    enum Version { FORGE, FABRIC }

    public static Installer INSTANCE;
    public static final Image BACKGROUND = getImage("background.png");
    public static final JSONObject JSON_OBJECT = getJSONObject();
    public static final String FORGE_MCVERSION = getMinecraftVersion(Version.FORGE);
    public static final String FABRIC_MCVERSION = getMinecraftVersion(Version.FABRIC);

    public static final int WINDOW_WIDTH = 700;
    public static final int WINDOW_HEIGHT = 500;

    private static JPanel panel;

    Installer() {
        INSTANCE = this;

        // set window properties
        setTitle("Ares Installer");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // add select version panel
        panel = new SelectVersionPanel();
        add(panel);

        setVisible(true);
    }

    public static void main(String[] args) {
        new Installer();
    }

    void select(Version version) {
        remove(panel);
        panel = new InstallPanel(version);
        add(panel);
        revalidate();

        System.out.println("Installing Ares " + version.name().toLowerCase() + "...");
    }

    private static JSONObject getJSONObject() {
        JSONObject jsonObject = null;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/4.76");
            JSONObject parent = new JSONObject(new JSONTokener(connection.getInputStream()));
            if(parent.has("versions")) jsonObject = parent.getJSONObject("versions");
        } catch(Exception ignored) {
        }

        if(jsonObject == null) {
            System.out.println("Error connecting to Ares download server! Check your internet connection");
            System.exit(1);
        }

        return jsonObject;
    }

    private static String getMinecraftVersion(Version version) {
        String name = version.name().toLowerCase();
        if(JSON_OBJECT.has(name)) {
            JSONObject versionObject = JSON_OBJECT.getJSONObject(name);
            if(versionObject.has("version")) return versionObject.getString("version");
        } else {
            System.out.println("Error connecting to Ares download server!");
            System.exit(1);
        }

        return name;
    }

    public static Image getImage(String name) {
        try {
            return ImageIO.read(SelectVersionPanel.class.getResourceAsStream("/assets/ares/installer/" + name));
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Image getImage(String name, int width, int height) {
        return getImage(name).getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
}
