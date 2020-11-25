package dev.tigr.ares.core.util.global;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.tigr.ares.core.Ares;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

/**
 * @author Tigermouthbear
 */
public class MojangApi {
    //<UUID, Username>
    private static final BiMap<String, String> playerCache = HashBiMap.create();

    public static String getUuid(String name) {
        if(playerCache.containsValue(name)) return playerCache.inverse().get(name);

        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            JSONObject jsonObject = new JSONObject(new JSONTokener(new InputStreamReader(url.openStream())));

            String uuid = jsonObject.get("id").toString();
            playerCache.put(uuid, name);
            return uuid;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUsername(String uuid) {
        if(uuid.contains("-")) uuid = uuid.replaceAll("-", "");

        if(playerCache.containsKey(uuid)) return playerCache.get(uuid);

        try {
            URL url = new URL("https://api.mojang.com/user/profiles/" + uuid + "/names");
            JSONArray jsonArray = new JSONArray(new JSONTokener(new InputStreamReader(url.openStream())));

            String name = ((JSONObject) jsonArray.get(jsonArray.length() - 1)).get("name").toString();
            playerCache.put(uuid, name);
            return name;
        } catch(Exception e) {
            Ares.LOGGER.info("UUID: " + uuid + " is not a valid account");
        }
        return null;
    }

    public static UUID stringToUUID(String uuid) {
        StringBuilder sb = new StringBuilder(uuid);
        sb.insert(8, '-');
        sb.insert(13, '-');
        sb.insert(18, '-');
        sb.insert(23, '-');
        return UUID.fromString(sb.toString());
    }
}
