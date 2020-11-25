package dev.tigr.ares.fabric.impl.modules.player;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.StringSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.fabric.event.client.OpenScreenEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import dev.tigr.simpleevents.listener.Priority;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

/**
 * @author Tigermouthbear
 * updated to 1.16.2 on 9/5/20
 */
@Module.Info(name = "AutoSign", description = "Place a sign automatically with text", category = Category.PLAYER)
public class AutoSign extends Module {
    private final Setting<String> line1 = register(new StringSetting("Line 1", ""));
    private final Setting<String> line2 = register(new StringSetting("Line 2", "Ares Client"));
    private final Setting<String> line3 = register(new StringSetting("Line 3", "on top!"));
    private final Setting<String> line4 = register(new StringSetting("Line 4", ""));

    @EventHandler
    public EventListener<OpenScreenEvent> openGuiEvent = new EventListener<>(Priority.HIGHEST, event -> {
        if(event.getScreen() instanceof SignEditScreen) {
            event.setCancelled(true);

            SignBlockEntity signBlockEntity = ReflectionHelper.getPrivateValue(SignEditScreen.class, event.getScreen(), "field_3031", "sign");
            if(signBlockEntity == null) return;
            MC.player.networkHandler.sendPacket(new UpdateSignC2SPacket(
                    signBlockEntity.getPos(),
                    line1.getValue().length() > 90 ? line1.getValue().substring(0, 90) : line1.getValue(),
                    line2.getValue().length() > 90 ? line2.getValue().substring(0, 90) : line2.getValue(),
                    line3.getValue().length() > 90 ? line3.getValue().substring(0, 90) : line3.getValue(),
                    line4.getValue().length() > 90 ? line4.getValue().substring(0, 90) : line4.getValue()
            ));
        }
    });
}
