package dev.tigr.ares.forge.impl.modules.render;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.forge.event.events.optimizations.CapeEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tigermouthbear 7/24/20
 */
@Module.Info(name = "Capes", description = "Shows Ares capes", category = Category.RENDER, enabled = true, visible = false)
public class Capes extends Module {
    private static final Map<String, Cape> CAPE_MAP = load("https://aresclient.org/capes/users.json");

    @EventHandler
    public EventListener<CapeEvent> capeEvent = new EventListener<>(event -> {
        String uuid = event.getPlayerInfo().getGameProfile().getId().toString().replaceAll("-", "");
        if(CAPE_MAP.containsKey(uuid)) {
            Cape cape = CAPE_MAP.get(uuid);
            if(cape.isRainbow()) {
                Color color = IRenderer.rainbow();
                GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            }
            if(cape.getResourceLocation() != null) event.getCir().setReturnValue(cape.getResourceLocation());
        }
    });

    // returns map<uuid, cape>
    private static Map<String, Cape> load(String database) {
        Map<String, Cape> map = new HashMap<>();

        // try to read the capes from the website
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(new JSONTokener(Utils.openURLStream(database)));
        } catch(IOException e) {
            return map;
        }

        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject capeObject = jsonArray.getJSONObject(i);

            // if if doesnt have the url and uuids skip it
            if(!capeObject.has("url") && !capeObject.has("uuids")) continue;

            // create cape
            String url = capeObject.getString("url");
            boolean rainbow = capeObject.has("rainbow") && capeObject.getBoolean("rainbow");
            Cape cape = new Cape(url, rainbow);

            // put the cape for all users
            capeObject.getJSONArray("uuids").forEach(oUUID -> {
                String uuid = ((String) oUUID).replaceAll("-", "");
                map.put(uuid, cape);
            });
        }

        return map;
    }
}

class Cape implements Wrapper {
    private final String url;
    private final boolean rainbow;

    private ResourceLocation resourceLocation = null;
    private boolean downloaded = false;

    Cape(String url, boolean rainbow) {
        this.url = url;
        this.rainbow = rainbow;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public ResourceLocation getResourceLocation() {
        if(resourceLocation == null && !downloaded) {
            // download cape if it hasn't been downloaded yet
            DynamicTexture dynamicTexture;
            try {
                BufferedImage image = ImageIO.read(Utils.openURLStream(url));
                dynamicTexture = new DynamicTexture(image);
                dynamicTexture.loadTexture(MC.getResourceManager());
            } catch(Exception ignored) {
                downloaded = true;
                return null;
            }

            resourceLocation = MC.getTextureManager().getDynamicTextureLocation(url, dynamicTexture);
        }

        return resourceLocation;
    }
}