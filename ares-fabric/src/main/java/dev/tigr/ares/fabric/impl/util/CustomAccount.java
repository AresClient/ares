package dev.tigr.ares.fabric.impl.util;

import com.mojang.authlib.Agent;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.util.UUIDTypeAdapter;
import dev.tigr.ares.core.util.AbstractAccount;
import dev.tigr.ares.fabric.impl.render.CustomRenderStack;
import dev.tigr.ares.fabric.mixin.accessors.MinecraftClientAccessor;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.Session;
import net.minecraft.util.math.Matrix4f;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import static dev.tigr.ares.Wrapper.MC;
import static dev.tigr.ares.Wrapper.RENDER_STACK;

/**
 * @author Tigermouthbear 11/24/20
 */
public class CustomAccount extends AbstractAccount {
    private static final AuthenticationService AUTHENTICATION_SERVICE = new YggdrasilAuthenticationService(MC.getNetworkProxy(), UUID.randomUUID().toString());
    private static final UserAuthentication USER_AUTHENTICATION = AUTHENTICATION_SERVICE.createUserAuthentication(Agent.MINECRAFT);

    // Stores the texture of the face of the user
    private AbstractTexture texture = null;

    public CustomAccount(String email, String password, String uuid) throws IOException {
        super(email, password, uuid);
        loadTexture();
    }

    public CustomAccount(String email, String password) throws IOException, AuthenticationException {
        super(email, password);
        loadTexture();
    }

    private void loadTexture() {
        // load image of head
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(ImageIO.read(new URL("https://crafatar.com/avatars/" + uuid)), "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            texture = new NativeImageBackedTexture(NativeImage.read(is));
        } catch(IOException ignored) {
        }
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

        ((MinecraftClientAccessor) MC).setSession(session);
        return true;
    }

    @Override
    public boolean isLoggedIn() {
        return MC.getSession().getProfile().getName().equals(name);
    }

    @Override
    public void drawHead(double x, double y, double width, double height) {
        if(texture == null) return;

        // bind texture
        RenderSystem.enableTexture();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture.getGlId());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // draw it
        Matrix4f matrix4f = ((CustomRenderStack)RENDER_STACK).getMatrixStack().peek().getModel();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f, (float) (x + width), (float) y, 0).texture(1, 0).next();
        bufferBuilder.vertex(matrix4f, (float) x, (float) y, 0).texture(0, 0).next();
        bufferBuilder.vertex(matrix4f, (float) x, (float) (y + height), 0).texture(0, 1).next();
        bufferBuilder.vertex(matrix4f, (float) x, (float) (y + height), 0).texture(0, 1).next();
        bufferBuilder.vertex(matrix4f, (float) (x + width), (float) (y + height), 0).texture(1, 1).next();
        bufferBuilder.vertex(matrix4f, (float) (x + width), (float) y, 0).texture(1, 0).next();
        Tessellator.getInstance().draw();
    }
}
