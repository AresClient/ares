package dev.tigr.ares.fabric.impl.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.Utils;
import dev.tigr.ares.core.util.render.Color;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.fabric.event.render.CapeColorEvent;
import dev.tigr.ares.fabric.event.render.CapeEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tigermouthbear 9/8/20
 */
@Module.Info(name = "Capes", description = "Renders custom capes for certain users", enabled = true, visible = false, category = Category.MISC)
public class Capes extends Module {
    private static final Map<String, Cape> CAPE_MAP = load("https://aresclient.org/capes/users.json");

    @EventHandler
    public EventListener<CapeEvent> capeEvent = new EventListener<>(event -> {
        String uuid = event.getPlayerInfo().getProfile().getId().toString().replaceAll("-", "");
        if(CAPE_MAP.containsKey(uuid)) {
            Cape cape = CAPE_MAP.get(uuid);
            if(cape.isRainbow()) {
                Color color = IRenderer.rainbow();
                RenderSystem.color4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            }
            if(cape.getIdentifier() != null) event.getCir().setReturnValue(cape.getIdentifier());
        }
    });

    @EventHandler
    public EventListener<CapeColorEvent> capeColorEvent = new EventListener<>(event -> {
        String uuid = event.getPlayerEntity().getGameProfile().getId().toString().replaceAll("-", "");
        if(CAPE_MAP.containsKey(uuid)) {
            Cape cape = CAPE_MAP.get(uuid);
            if(cape.isRainbow()) event.setColor(IRenderer.rainbow());
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

    private Identifier identifier = null;
    private boolean downloaded = false;

    Cape(String url, boolean rainbow) {
        this.url = url;
        this.rainbow = rainbow;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public Identifier getIdentifier() {
        if(identifier == null && !downloaded) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(ImageIO.read(Utils.openURLStream(url)), "png", os);
                InputStream is = new ByteArrayInputStream(os.toByteArray());
                identifier = MC.getTextureManager().registerDynamicTexture(url.substring(url.lastIndexOf("/") + 1), new NativeImageBackedTexture(NativeImage.read(is)));
            } catch(IOException ignored) {
            }
            downloaded = true;
        }

        return identifier;
    }
}