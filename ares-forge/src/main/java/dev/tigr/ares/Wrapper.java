package dev.tigr.ares;

import net.minecraft.client.Minecraft;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Tigermouthbear
 */
public interface Wrapper extends CoreWrapper {
    Minecraft MC = Minecraft.getMinecraft();
    Executor EXECUTOR = Executors.newCachedThreadPool();
}