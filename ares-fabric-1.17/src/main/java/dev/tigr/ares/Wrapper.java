package dev.tigr.ares;

import net.minecraft.client.MinecraftClient;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public interface Wrapper extends CoreWrapper {
    MinecraftClient MC = MinecraftClient.getInstance();
    Executor EXECUTOR = Executors.newCachedThreadPool();
}
