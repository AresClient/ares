package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ConnectScreen.class)
public interface ConnectScreenAccessor {
    @Invoker("connect")
    void connect(final MinecraftClient client, final ServerAddress address);
}
