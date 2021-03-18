package dev.tigr.ares.installer;

import net.fabricmc.installer.client.ClientInstaller;
import net.fabricmc.installer.client.ProfileInstaller;
import net.fabricmc.installer.util.InstallerProgress;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Tigermouthbear
 */
public class LoaderInstaller {
    public static boolean install(String gameVersion, File folder) throws Exception {
        if(gameVersion.equals("1.12.2")) return false; // no automatic forge installation
        else return installFabricLoader(folder, gameVersion);
    }

    private static boolean installFabricLoader(File minecraftFolder, String gameVersion) throws IOException {
        String loaderVersion = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://meta.fabricmc.net/v2/versions/loader").openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/4.76");

            JSONArray jsonArray = new JSONArray(new JSONTokener(connection.getInputStream()));
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                if(jsonObject.has("stable") && jsonObject.getBoolean("stable") && jsonObject.has("version"))
                    loaderVersion = jsonObject.getString("version");
            }
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        if(loaderVersion == null) return false;

        String profileName = ClientInstaller.install(minecraftFolder.toPath(), gameVersion, loaderVersion, InstallerProgress.CONSOLE);
        ProfileInstaller.setupProfile(minecraftFolder.toPath(), profileName, gameVersion);
        return true;
    }
}
