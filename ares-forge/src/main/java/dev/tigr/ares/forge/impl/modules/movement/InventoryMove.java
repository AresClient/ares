package dev.tigr.ares.forge.impl.modules.movement;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.events.movement.InventoryMoveEvent;
import dev.tigr.simpleevents.event.Result;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "InventoryMove", description = "Allows you to move while in your inventory", category = Category.MOVEMENT)
public class InventoryMove extends Module {
    @EventHandler
    public EventListener<InventoryMoveEvent> inventoryMoveEvent = new EventListener<>(event -> event.setResult(Result.ALLOW));

    @Override
    public void onTick() {
        if(MC.currentScreen instanceof GuiChat || MC.currentScreen == null) return;

        if(Keyboard.isKeyDown(Keyboard.KEY_UP)) MC.player.rotationPitch -= 4;
        if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) MC.player.rotationPitch += 4;
        if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) MC.player.rotationYaw -= 4;
        if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) MC.player.rotationYaw += 4;
    }
}
