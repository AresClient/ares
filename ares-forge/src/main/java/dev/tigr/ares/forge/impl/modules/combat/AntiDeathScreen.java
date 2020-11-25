package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;

import javax.annotation.Nullable;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AntiDeathScreen", description = "Prevents death screen from showing if player is still alive", category = Category.COMBAT)
public class AntiDeathScreen extends Module {
    @EventHandler
    public EventListener<GuiOpenEvent> openGuiEvent = new EventListener<>(event -> {
        if(event.getGui() instanceof GuiGameOver) {
            ITextComponent causeOfDeath = ReflectionHelper.getPrivateValue(GuiGameOver.class, (GuiGameOver) event.getGui(), "causeOfDeath", "field_184871_f");
            event.setGui(new GameOverGui(causeOfDeath));
        }
    });
}

class GameOverGui extends GuiGameOver implements Wrapper {
    GameOverGui(@Nullable ITextComponent causeOfDeath) {
        super(causeOfDeath);
    }

    @Override
    public void updateScreen() {
        // If the player is actually alive, yet we're still playing this death screen for some reason
        if(!MC.player.isDead && MC.player.getHealth() > 0) {
            // Remove the death screen!
            this.MC.displayGuiScreen(null);
            this.MC.setIngameFocus();
        } else {
            super.updateScreen();
        }
    }
}
