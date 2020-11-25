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
        try {
            JSONObject jsonObject = new JSONObject(new JSONTokener(Utils.openURLStream(URL)));

            if(jsonObject.has(Ares.MC_VERSION)) {
                JSONObject versionObject = jsonObject.getJSONObject(Ares.MC_VERSION);
                if(versionObject.has(Ares.BRANCH.name().toLowerCase())) {
                    JSONObject branchObject = versionObject.getJSONObject(Ares.BRANCH.name().toLowerCase());
                    if(branchObject.has("name")) {
                        latestVersion = branchObject.getString("name");
                    }
                }
            }

            return !latestVersion.equals(Ares.VERSION);
        } catch(Exception ignored) {
            latestVersion = Ares.VERSION;
            Ares.LOGGER.info("Failed to query latest version!");
        }
        return false;
    }

    public static String getLatestVersion() {
        return Ares.BRANCH == Ares.Branches.STABLE ? latestVersion : (Ares.BRANCH == Ares.Branches.PLUS ? latestVersion.concat("+") : latestVersion.concat(" BETA"));
    }
}
