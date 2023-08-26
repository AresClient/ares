package org.aresclient.ares.api.minecraft;

import org.aresclient.ares.api.minecraft.render.Framebuffer;
import org.aresclient.ares.api.minecraft.render.Renderer;
import org.aresclient.ares.api.minecraft.render.Screen;
import org.aresclient.ares.api.minecraft.util.Profiler;
import org.aresclient.ares.api.minecraft.util.Session;
import org.aresclient.ares.api.render.Resolution;

public interface Minecraft {
    Profiler getProfiler();

    Session getSession();

    void openScreen(Screen screen);
    void closeScreen();

    Resolution getResolution();

    Framebuffer getFramebuffer();

    Renderer getRenderer();

    void shutdown();

    boolean isInWorld();
}
