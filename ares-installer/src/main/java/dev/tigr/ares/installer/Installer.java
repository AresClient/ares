package dev.tigr.ares.installer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

/**
 * @author Tigermouthbear
 */
public class Installer extends JFrame {
    private static final String URL = "https://aresclient.org/api/v1/downloads.json";

    enum Loader { FORGE, FABRIC }
    static class Candidate {
        private final String name;
        private final Loader loader;
        private final String version; // minecraft version
        private final String url;

        public Candidate(String name, Loader loader, String version, String url) {
            this.name = name;
            this.loader = loader;
            this.version = version;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public Loader getLoader() {
            return loader;
        }

        public String getVersion() {
            return version;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return version + " " + loader.name().toLowerCase();
        }
    }

    public static Installer INSTANCE;
    public static final Image BACKGROUND = getImage("background.png");
    public static final JSONObject JSON_OBJECT = getJSONObjectFromURL(URL);
    public static final JSONObject FORGE_OBJECT = getObjectOrExit(JSON_OBJECT, Loader.FORGE.name().toLowerCase());
    public static final JSONObject FABRIC_OBJECT = getObjectOrExit(JSON_OBJECT, Loader.FABRIC.name().toLowerCase());
    public static final ArrayList<Candidate> CANDIDATES = new ArrayList<>();
    static {
        getArrayOrExit(FORGE_OBJECT, "all").forEach(str -> {
            if(!(str instanceof String)) return;
            JSONObject version = getObjectOrExit(FORGE_OBJECT, (String) str);
            CANDIDATES.add(new Candidate(getStringOrExit(version, "name"), Loader.FORGE, (String) str, getStringOrExit(version, "url")));
        });
        getArrayOrExit(FABRIC_OBJECT, "all").forEach(str -> {
            if(!(str instanceof String)) return;
            JSONObject version = getObjectOrExit(FABRIC_OBJECT, (String) str);
            CANDIDATES.add(new Candidate(getStringOrExit(version, "name"), Loader.FABRIC, (String) str, getStringOrExit(version, "url")));
        });
    }

    public static final int WINDOW_WIDTH = 650;
    public static final int WINDOW_HEIGHT = 500;

    private static JPanel panel;

    Installer() {
        INSTANCE = this;

        // set window properties
        setTitle("Ares Installer");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(getImage("icon.png"));
        setResizable(false);

        // add select version panel
        panel = new InstallPanel(CANDIDATES);
        add(panel);

        setVisible(true);
    }

    public static void main(String[] args) {
        // enable anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");

        new Installer();
    }

    public static String install(Candidate candidate, File folder) {
        // only check for loader if not multimc
        if(!folder.getPath().contains("multimc")) {
            // lambda for checking if file is loader for forge or fabric
            Function<String, Boolean> tester = candidate.getLoader() == Loader.FABRIC
                    ? file -> file.startsWith("fabric-loader-") && file.endsWith(candidate.getVersion())
                    : file -> file.startsWith(candidate.getVersion() + "-forge-14.23.5.");

            // find versions folder
            File versions = new File(folder, "versions");
            if(!versions.exists()) return "Looks like there's something wrong with your minecraft folder! Make sure you selected the correct location";

            // install minecraft forge or fabric if not installed
            if(Arrays.stream(versions.listFiles()).noneMatch(file -> file.isDirectory() && tester.apply(file.getName()))) {
                String err = candidate.getLoader() == Loader.FABRIC ? "Please install minecraft fabric for " + candidate.getVersion() + " at https://fabricmc.net" : "Please install minecraft forge for " + candidate.getVersion() + " at https://files.minecraftforge.net/";
                try {
                    if(!LoaderInstaller.install(candidate, folder)) return err;
                } catch(Exception e) {
                    e.printStackTrace();
                    return err;
                }
            }
        }

        // create\check mods folder
        File mods = new File(folder, "mods");
        if(!mods.exists() || !mods.isDirectory()) mods.mkdir();

        // create file
        File out = new File(mods, "Ares-" + candidate.getName() + "-" + candidate.getVersion() + ".jar");
        if(!out.exists()) {
            String err = "Error installing Ares client! Visit our website for the faq and link to discord for support";
            try {
                if(!out.createNewFile()) return err;
            } catch(IOException e) {
                e.printStackTrace();
                return err;
            }
        } else return "Ares " + candidate.getName() + " " + candidate.getVersion() +" is already installed!";

        // remove old versions from mods folder
        Arrays.stream(mods.listFiles()).filter(file -> {
            String name = file.getName();
            if(name.equals(out.getName())) return false;
            if(candidate.getLoader() == Loader.FABRIC) return (name.startsWith("Ares-") && name.endsWith(".jar") && !name.contains("1.12.2")) || // stable
                    name.startsWith("ares-fabric-"); // beta
            if(candidate.getLoader() == Loader.FORGE) return name.startsWith("Ares-") && name.endsWith("-1.12.2.jar") || // stable
                    name.startsWith("ares-forge-"); // beta

            return false;
        }).forEach(File::delete);

        // download file to mods folder
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(candidate.getUrl()).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/4.76");

            FileOutputStream fos = new FileOutputStream(out);
            fos.getChannel().transferFrom(Channels.newChannel(connection.getInputStream()), 0, Long.MAX_VALUE);
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
            return "Error downloading Ares Client! Check your internet connection.";
        }

        return "Successfully installed Ares " + candidate.getName() + " for " + candidate.getLoader().name().toLowerCase() + " " + candidate.getVersion() + " to " + folder.getPath();
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

    private static JSONObject getJSONObjectFromURL(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/4.76");
            JSONObject parent = new JSONObject(new JSONTokener(connection.getInputStream()));
            if(parent.has("versions")) return parent.getJSONObject("versions");
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("Error connecting to Ares download server! Check your internet connection");
        System.exit(1);
        return null;
    }

    private static JSONObject getObjectOrExit(JSONObject jsonObject, String name) {
        if(jsonObject.has(name)) {
            try {
                return jsonObject.getJSONObject(name);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Error reading download json!");
        System.exit(1);
        return null;
    }

    private static JSONArray getArrayOrExit(JSONObject jsonObject, String name) {
        if(jsonObject.has(name)) {
            try {
                return jsonObject.getJSONArray(name);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Error reading download json!");
        System.exit(1);
        return null;
    }

    private static String getStringOrExit(JSONObject jsonObject, String name) {
        if(jsonObject.has(name)) {
            try {
                return jsonObject.getString(name);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Error reading download json!");
        System.exit(1);
        return null;
    }

    public static Image getImage(String name) {
        try {
            return ImageIO.read(Installer.class.getResourceAsStream("/assets/ares/installer/" + name));
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
