package dev.tigr.ares.installer;

import org.json.JSONObject;
import org.json.JSONTokener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.function.Function;

/**
 * @author Tigermouthbear
 */
public class Installer extends JFrame {
    private static final String URL = "https://aresclient.org/api/v1/downloads.json";

    enum Version { FORGE, FABRIC }

    public static Installer INSTANCE;
    public static final Image BACKGROUND = getImage("background.png");
    public static final JSONObject JSON_OBJECT = getJSONObject();
    public static final String FORGE_MCVERSION = getMinecraftVersionFromJson(Version.FORGE);
    public static final String FABRIC_MCVERSION = getMinecraftVersionFromJson(Version.FABRIC);
    public static final String FORGE_VERSION = getVersionFromJson(Version.FORGE);
    public static final String FABRIC_VERSION = getVersionFromJson(Version.FABRIC);
    public static final String FORGE_URL = getURLFromJson(Version.FORGE);
    public static final String FABRIC_URL = getURLFromJson(Version.FABRIC);

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
        // enable anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");

        new Installer();
    }

    void select(Version version) {
        remove(panel);
        panel = new InstallPanel(version);
        add(panel);
        revalidate();
    }

    public static String install(Version version, File folder) {
        try {
            // lambda for checking if file is loader for forge or fabric
            Function<String, Boolean> tester = version == Version.FABRIC
                    ? file -> file.startsWith("fabric-loader-") && file.endsWith(FABRIC_MCVERSION)
                    : file -> file.startsWith(FORGE_MCVERSION + "-forge-14.23.5.");

            if(Files.walk(folder.toPath()).noneMatch(file -> tester.apply(file.toFile().getName()))) {
                // TODO: AUTOMATIC FORGE/FABRIC INSTALLER?
                // tell user to install forge or fabric loader if missing
                return version == Version.FABRIC ? "Please install minecraft fabric for " + FABRIC_MCVERSION + " at https://fabricmc.net" : "Please install minecraft forge for " + FORGE_MCVERSION + " at https://files.minecraftforge.net/";
            }
        } catch(IOException e) {
            e.printStackTrace();
            return "Error installing Ares client! Visit our website for the faq and link to discord for support";
        }

        // create\check mods folder
        File mods = new File(folder, "mods");
        if(!mods.exists() || !mods.isDirectory()) mods.mkdir();

        // TODO: REMOVE OLDER VERSIONS FROM MODS FOLDER

        // create file
        File file = new File(mods, "Ares-" + getVersion(version) + "-" + getMinecraftVersion(version) + ".jar");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(IOException e) {
                e.printStackTrace();
                return "Error installing Ares client! Visit our website for the faq and link to discord for support";
            }
        } else return "Ares " + getVersion(version) + " " + getMinecraftVersion(version) +" is already installed!";

        // download file
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(getURL(version)).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/4.76");

            InputStream is = connection.getInputStream();
            byte[] bytes = new byte[connection.getContentLength()];
            is.read(bytes);
            is.close();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
            return "Error downloading Ares Client! Check your internet connection.";
        }

        return "Successfully installed Ares to " + folder.getPath();
    }
    
    public static String getMinecraftFolder() {
        if(System.getProperty("os.name").toLowerCase().contains("nux")) {
            return System.getProperty("user.home") + "/.minecraft/";
        } else if(System.getProperty("os.name").toLowerCase().contains("darwin") || System.getProperty("os.name").toLowerCase().contains("mac")) {
            return System.getProperty("user.home") + "/Library/Application Support/minecraft/";
        } else if(System.getProperty("os.name").toLowerCase().contains("win")) {
            return System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator;
        } else return null;
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

    public static String getMinecraftVersion(Version version) {
        return version == Version.FABRIC ? FABRIC_MCVERSION : FORGE_MCVERSION;
    }

    private static String getMinecraftVersionFromJson(Version version) {
        String name = version.name().toLowerCase();
        if(JSON_OBJECT.has(name)) {
            JSONObject versionObject = JSON_OBJECT.getJSONObject(name);
            if(versionObject.has("version")) return versionObject.getString("version");
        } else {
            System.out.println("Error reading download json!");
            System.exit(1);
        }

        return name;
    }

    public static String getVersion(Version version) {
        return version == Version.FABRIC ? FABRIC_VERSION : FORGE_VERSION;
    }

    private static String getVersionFromJson(Version version) {
        String name = version.name().toLowerCase();
        if(JSON_OBJECT.has(name)) {
            JSONObject versionObject = JSON_OBJECT.getJSONObject(name);
            if(versionObject.has("name")) return versionObject.getString("name");
        } else {
            System.out.println("Error reading download json!");
            System.exit(1);
        }

        return null;
    }

    public static String getURL(Version version) {
        return version == Version.FABRIC ? FABRIC_URL : FORGE_URL;
    }

    private static String getURLFromJson(Version version) {
        String name = version.name().toLowerCase();
        if(JSON_OBJECT.has(name)) {
            JSONObject versionObject = JSON_OBJECT.getJSONObject(name);
            if(versionObject.has("url")) return versionObject.getString("url");
        } else {
            System.out.println("Error reading download json!");
            System.exit(1);
        }

        return null;
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
