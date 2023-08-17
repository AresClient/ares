package org.aresclient.ares;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DemoScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import org.aresclient.ares.api.minecraft.Minecraft;
import org.aresclient.ares.api.minecraft.math.*;
import org.aresclient.ares.impl.minecraft.MinecraftMesh;
import org.aresclient.ares.impl.minecraft.math.Vec3fMesh;
import org.joml.Vector3f;

public class AresStatics {
    public static Minecraft getMinecraft() {
        return new MinecraftMesh(MinecraftClient.getInstance());
    }

    public static Box createBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        return (Box) new net.minecraft.util.math.Box(x1, y1, z1, x2, y2, z2);
    }

    public static Vec2f createVec2f(float x, float y) {
        return (Vec2f) new net.minecraft.util.math.Vec2f(x, y);
    }

    public static Vec3d createVec3d(double x, double y, double z) {
        return (Vec3d) new net.minecraft.util.math.Vec3d(x, y, z);
    }

    public static Vec3f createVec3f(float x, float y, float z) {
        return new Vec3fMesh(new Vector3f(x, y, z));
    }

    public static Vec3i createVec3i(int x, int y, int z) {
        return (Vec3i) new net.minecraft.util.math.Vec3i(x, y, z);
    }

    public static BlockPos createBlockPos(int x, int y, int z) {
        return (BlockPos) new net.minecraft.util.math.BlockPos(x, y, z);
    }

    public static void openChatScreen(String input) {
        MinecraftClient.getInstance().setScreen(new ChatScreen(input));
    }

    public static void openDemoScreen() {
        MinecraftClient.getInstance().setScreen(new DemoScreen());
    }

    public static void openMultiplayerScreen() {
        MinecraftClient.getInstance().setScreen(new MultiplayerScreen(MinecraftClient.getInstance().currentScreen));
    }

    public static void openOptionsScreen() {
        MinecraftClient.getInstance().setScreen(new OptionsScreen(MinecraftClient.getInstance().currentScreen, MinecraftClient.getInstance().options));
    }

    public static void openSelectWorldScreen() {
        MinecraftClient.getInstance().setScreen(new SelectWorldScreen(MinecraftClient.getInstance().currentScreen));
    }

    public static void openRealmsMainScreen() {
        MinecraftClient.getInstance().setScreen(new RealmsMainScreen(MinecraftClient.getInstance().currentScreen));
    }

    public static void openTitleScreen() {
        MinecraftClient.getInstance().setScreen(new TitleScreen());
    }
}
