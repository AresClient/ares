package dev.tigr.ares.fabric.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.event.movement.InventoryMoveEvent;
import dev.tigr.simpleevents.event.Result;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "InventoryMove", description = "Allows you to move while in your inventory", category = Category.MOVEMENT)
public class InventoryMove extends Module {
    @EventHandler
    public EventListener<InventoryMoveEvent> inventoryMoveEvent = new EventListener<>(event -> event.setResult(Result.ALLOW));

    @Override
    public void onTick() {
        if(MC.currentScreen instanceof ChatScreen || MC.currentScreen == null) return;
        if(InputUtil.isKeyPressed(MC.getWindow().getHandle(), GLFW.GLFW_KEY_UP)) MC.player.pitch -= 4;
        if(InputUtil.isKeyPressed(MC.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN)) MC.player.pitch += 4;
        if(InputUtil.isKeyPressed(MC.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT)) MC.player.yaw -= 4;
        if(InputUtil.isKeyPressed(MC.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT)) MC.player.yaw += 4;
    }
}
