package dev.tigr.ares.forge.impl.util;

import com.mojang.authlib.Agent;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;
import dev.tigr.ares.core.util.AbstractAccount;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Session;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.UUID;

import static dev.tigr.ares.Wrapper.MC;

/**
 * @author Tigermouthbear
 */
public class CustomAccount extends AbstractAccount {
    private static final AuthenticationService AUTHENTICATION_SERVICE = new YggdrasilAuthenticationService(MC.getProxy(), UUID.randomUUID().toString());
    private static final UserAuthentication USER_AUTHENTICATION = AUTHENTICATION_SERVICE.createUserAuthentication(Agent.MINECRAFT);
    private static final MinecraftSessionService MINECRAFT_SESSION_SERVICE = AUTHENTICATION_SERVICE.createMinecraftSessionService();

    // Stores the texture of the face of the user
    private DynamicTexture texture;

    public CustomAccount(String email, String password, String uuid) throws IOException {
        super(email, password, uuid);
        loadTexture();
    }

    public CustomAccount(String email, String password) throws AuthenticationException, IOException {
        super(email, password);
        loadTexture();
    }

    private void loadTexture() {
        DynamicTexture texture;
        try {
            texture = new DynamicTexture(ImageIO.read(new URL("https://crafatar.com/avatars/" + uuid)));
        } catch(IOException e) {
            texture = null;
        }
        this.texture = texture;
    }

    @Override
    protected String getUUID(String email, String password) throws AuthenticationException {
        // logout of authentication
        USER_AUTHENTICATION.logOut();

        // set credentials
        USER_AUTHENTICATION.setUsername(email);
        USER_AUTHENTICATION.setPassword(password);

        // log into account
        USER_AUTHENTICATION.logIn();

        String uuid = USER_AUTHENTICATION.getSelectedProfile().getId().toString().replaceAll("-", "");

        // logout of account
        USER_AUTHENTICATION.logOut();

        return uuid;
    }

    @Override
    public boolean login() throws AuthenticationException {
        if(isLoggedIn()) return true;

        // logout of authentication
        USER_AUTHENTICATION.logOut();

        // set credentials
        USER_AUTHENTICATION.setUsername(email);
        USER_AUTHENTICATION.setPassword(password);

        // log into account
        USER_AUTHENTICATION.logIn();

        // create new session
        Session session = new Session(
                USER_AUTHENTICATION.getSelectedProfile().getName(),
                UUIDTypeAdapter.fromUUID(USER_AUTHENTICATION.getSelectedProfile().getId()),
                USER_AUTHENTICATION.getAuthenticatedToken(),
                "mojang"
        );

        // find session field
        Field field;
        try {
            field = MC.getClass().getDeclaredField("session");
        } catch(NoSuchFieldException e) {
            try {
                field = MC.getClass().getDeclaredField("field_71449_j");
            } catch(NoSuchFieldException e1) {
                e1.printStackTrace();
                return false;
            }
        }

        // set field accessible then change to new session
        field.setAccessible(true);
        try {
            field.set(MC, session);
        } catch(IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean isLoggedIn() {
        return MC.getSession().getProfile().getName().equals(name);
    }

    @Override
    public void drawHead(double x, double y, double width, double height) {
        GL11.glColor4d(1, 1, 1, 1);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();

        // bind head texture
        GlStateManager.bindTexture(texture.getGlTextureId());

        // render head
        GL11.glBegin(GL11.GL_TRIANGLES);
        {
            GL11.glTexCoord2d(1, 0);
            GL11.glVertex2d(x + width, y);

            GL11.glTexCoord2d(0, 0);
            GL11.glVertex2d(x, y);

            GL11.glTexCoord2d(0, 1);
            GL11.glVertex2d(x, y + height);

            GL11.glTexCoord2d(0, 1);
            GL11.glVertex2d(x, y + height);

            GL11.glTexCoord2d(1, 1);
            GL11.glVertex2d(x + width, y + height);

            GL11.glTexCoord2d(1, 0);
            GL11.glVertex2d(x + width, y);
        }
        GL11.glEnd();
    }
}
