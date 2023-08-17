package org.aresclient.ares.impl.minecraft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.aresclient.ares.api.minecraft.AbstractMesh;
import org.aresclient.ares.api.minecraft.Minecraft;
import org.aresclient.ares.api.minecraft.render.Framebuffer;
import org.aresclient.ares.api.minecraft.render.Renderer;
import org.aresclient.ares.api.minecraft.render.Screen;
import org.aresclient.ares.api.minecraft.util.Profiler;
import org.aresclient.ares.api.minecraft.util.Session;
import org.aresclient.ares.impl.minecraft.render.RendererMesh;
import org.aresclient.ares.impl.minecraft.render.ScreenAdapter;
import org.aresclient.ares.impl.minecraft.util.ProfilerMesh;
import org.aresclient.ares.api.render.Resolution;

public class MinecraftMesh extends AbstractMesh<MinecraftClient> implements Minecraft {
    public MinecraftMesh(MinecraftClient value) {
        super(value);
    }

    @Override
    public Profiler getProfiler() {
        return new ProfilerMesh(getMeshValue().getProfiler());
    }

    @Override
    public Session getSession() {
        return (Session) getMeshValue().getSession();
    }

    @Override
    public void openScreen(Screen screen) {
        getMeshValue().setScreen(new ScreenAdapter(screen));
    }

    @Override
    public void closeScreen() {
        getMeshValue().setScreen(null);
    }

    @Override
    public Resolution getResolution() {
        Window window = getMeshValue().getWindow();
        return new Resolution(
                window.getWidth(),
                window.getHeight(),
                window.getScaledWidth(),
                window.getScaledHeight(),
                window.getScaleFactor()
        );
    }

    @Override
    public Framebuffer getFramebuffer() {
        return (Framebuffer) getMeshValue().getFramebuffer();
    }

    @Override
    public Renderer getRenderer() {
        return new RendererMesh(getMeshValue().gameRenderer);
    }

    @Override
    public void shutdown() {
        getMeshValue().scheduleStop();
    }
}
