package dev.tigr.ares.core.util.global;

import dev.tigr.ares.core.Ares;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Tigermouthbear
 */
public class UpdateHelper {
    private static final String URL = "https://aresclient.org/api/v1/downloads.json";
    private static String latestVersion = null;

    public static boolean shouldUpdate() {
        if(Ares.BRANCH == Ares.Branches.BETA) return false;

        try {
            JSONObject jsonObject = new JSONObject(new JSONTokener(Utils.openURLStream(URL)));

            if(jsonObject.has("versions")) {
                JSONObject versions = jsonObject.getJSONObject("versions");
                if(versions.has(Ares.MC_VERSION)) {
                    JSONObject version = versions.getJSONObject(Ares.MC_VERSION);
                    if(version.has("name")) {
                        latestVersion = version.getString("name");
                        return !latestVersion.equals(Ares.VERSION);
                    }
                }
            }

            return false;
        } catch(Exception ignored) {
            latestVersion = Ares.VERSION;
            Ares.LOGGER.info("Failed to query latest version!");
        }
        return false;
    }

    public static String getLatestVersion() {
        return latestVersion != null ? latestVersion : Ares.VERSION;
    }
}
